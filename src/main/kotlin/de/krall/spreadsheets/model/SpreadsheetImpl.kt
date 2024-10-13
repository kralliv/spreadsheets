package de.krall.spreadsheets.model

import de.krall.spreadsheets.grid.SparseGrid
import de.krall.spreadsheets.util.empty
import de.krall.spreadsheets.value.ComputationError
import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.EvaluatedValue
import de.krall.spreadsheets.value.ParsedValue
import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.ReferenceRange
import de.krall.spreadsheets.value.Referencing
import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.formula.Formula
import de.krall.spreadsheets.value.formula.ReferenceResolver
import de.krall.spreadsheets.value.parser.ValueParser
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

class SpreadsheetImpl : Spreadsheet {

    private val listeners = CopyOnWriteArrayList<SpreadsheetListener>()

    private val engine = SpreadsheetEngine(ValueParser(), lazyEvaluation = true) { column, row, _ ->
        val cell = CellImpl(column, row)

        listeners.forEach { it.cellChanged(cell) }
    }

    override fun get(column: Int, row: Int): Cell {
        return CellImpl(column, row)
    }

    private inner class CellImpl(
        override val column: Int,
        override val row: Int,
    ) : Cell {

        override var value: Value?
            get() = engine.read(column, row).value
            set(value) {
                engine.write(column, row) { it.copy(value = value) }
            }

        override val evaluatedValue: EvaluatedValue?
            get() = engine.read(column, row).evaluatedValue
    }

    override fun addListener(listener: SpreadsheetListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SpreadsheetListener) {
        listeners.remove(listener)
    }

    override fun close() {
        engine.close()
    }
}

private class SpreadsheetEngine(
    private val valueParser: ValueParser,
    private val lazyEvaluation: Boolean,
    private val changeListener: ChangeListener,
) : Closeable {

    private class Node(
        val column: Int,
        val row: Int,
        attributes: CellAttributes,
    ) {

        @Volatile
        private var _attributes: CellAttributes = attributes

        val attributes: CellAttributes
            get() = _attributes

        fun update(attributes: CellAttributes): CellAttributes {
            // atomicity is currently not required
            val previousAttributes = _attributes
            _attributes = attributes
            return previousAttributes
        }

        val dependencies = mutableSetOf<Node>()
        val dependents = mutableSetOf<Node>()

        fun addDependent(node: Node) {
            if (dependents.add(node)) {
                node.dependencies.add(this)
            }
        }

        fun removeDependent(node: Node) {
            if (dependents.remove(node)) {
                node.dependencies.remove(this)
            }
        }

        fun clearDependents(): Collection<Node> {
            val dependent = dependents.empty()
            for (node in dependent) {
                node.dependencies.remove(this)
            }
            return dependent
        }

        fun clearDependencies(): Collection<Node> {
            val dependencies = dependencies.empty()
            for (node in dependencies) {
                node.dependents.remove(this)
            }
            return dependencies
        }
    }

    private class BlankCells {

        private val references = mutableListOf<Pair<Node, Referencing>>()

        fun addDependent(node: Node, referencing: Referencing) {
            references.add(node to referencing)
        }

        fun clearDependents(node: Node) {
            references.removeIf { (candidate, _) -> candidate === node }
        }

        fun takeDependents(row: Int, column: Int): List<Node> {
            val dependencies = mutableListOf<Node>()
            val iterator = references.iterator()
            while (iterator.hasNext()) {
                val (node, referencing) = iterator.next()

                when (referencing) {
                    is Reference -> if (referencing.cell.contains(column, row)) {
                        iterator.remove()
                        dependencies.add(node)
                    }

                    is ReferenceRange -> if (referencing.area.contains(column, row)) {
                        // areas don't get removed here as there might
                        // be other blanks cell covered.
                        dependencies.add(node)
                    }
                }
            }
            return dependencies
        }
    }

    private class Notification(val column: Int, val row: Int, val attributes: CellAttributes)

    private val closed = AtomicBoolean(false)
    private val lock = ReentrantReadWriteLock()

    private val grid = SparseGrid<Node>()
    private val blankCells = BlankCells()

    private val evaluationQueue = LinkedBlockingDeque<Node>()
    private val evaluationThread = thread(name = "spreadsheet-engine-evaluation") {
        while (!closed.get()) {
            try {
                val node = evaluationQueue.take()

                lock.read {
                    evaluateNode(node)
                }
            } catch (t: Throwable) {
                t.printStackTrace() // TODO
            }
        }
    }

    private val notifyQueue = LinkedBlockingDeque<Notification>()
    private val notifyThread = thread(name = "spreadsheet-engine-notify") {
        while (!closed.get()) {
            try {
                val notification = notifyQueue.take()

                changeListener.cellChanged(notification.column, notification.row, notification.attributes)
            } catch (t: Throwable) {
                t.printStackTrace() // TODO
            }
        }
    }

    fun read(column: Int, row: Int): CellAttributes = lock.read {
        checkNotClosed()

        val node = grid[column, row]
        if (node != null && lazyEvaluation) {
            requestEvaluation(node)
        }
        return node?.attributes ?: CellAttributes.Blank
    }

    fun write(column: Int, row: Int, mutation: (CellAttributes) -> CellAttributes) = lock.write {
        checkNotClosed()

        val node = grid[column, row]

        val previousAttributes = node?.attributes ?: CellAttributes.Blank
        val mutatedAttributes = mutation(previousAttributes)
        val attributes = parseValue(mutatedAttributes, previousAttributes)

        if (attributes.isNotBlank()) {
            if (node == null) {
                insert(column, row, attributes)
            } else {
                update(node, attributes)
            }
        } else if (node != null) {
            remove(node)
        }
    }

    private fun parseValue(attributes: CellAttributes, previousAttributes: CellAttributes): CellAttributes {
        val valueUnchanged = attributes.value === previousAttributes.value
        val nullnessMatches = (attributes.value == null) == (attributes.parsedValue == null)
        if (valueUnchanged && nullnessMatches) return attributes

        val parsedValue = when (val value = attributes.value) {
            null -> null
            is Value.Text -> ParsedValue.Text(value.text)
            is Value.Number -> ParsedValue.Number(value.number)
            is Value.Formula -> when (val formula = valueParser.parseFormula(value.formula)) {
                null -> ParsedValue.BadFormula
                else -> ParsedValue.Formula(formula)
            }
        }

        return attributes.copy(parsedValue = parsedValue)
    }

    private fun fastEvaluateValue(attributes: CellAttributes): CellAttributes {
        val evaluatedValue = when (val value = attributes.parsedValue) {
            null -> null
            is ParsedValue.Text -> EvaluatedValue.Text(value.text)
            is ParsedValue.Number -> EvaluatedValue.Number(value.number)
            is ParsedValue.Formula -> EvaluatedValue.Unevaluated
            is ParsedValue.BadFormula -> EvaluatedValue.Error(ComputationError.BadFormula)
        }

        return attributes.copy(evaluatedValue = evaluatedValue)
    }

    private fun insert(column: Int, row: Int, attributes: CellAttributes): Node {
        val node = Node(column, row, attributes)
        node.dependents.addAll(blankCells.takeDependents(row, column))
        grid[row, column] = node

        invalidateNode(node)

        if (!lazyEvaluation) {
            requestEvaluation(node)
        }

        return node
    }

    private fun update(node: Node, attributes: CellAttributes) {
        val previousAttributes = node.update(attributes)

        if (previousAttributes.parsedValue !== attributes.parsedValue) {
            invalidateNode(node)

            if (!lazyEvaluation) {
                requestEvaluation(node)
            }
        } else {
            notifyNodeChanged(node)
        }
    }

    private fun remove(node: Node) {
        grid[node.row, node.column] = null
        node.update(CellAttributes.Blank)

        invalidateNode(node)

        cancelEvaluation(node)
    }

    private fun invalidateNode(node: Node) {
        var attributes = node.attributes
        attributes = fastEvaluateValue(attributes)
        node.update(attributes)

        // retract all dependencies
        node.clearDependencies()
        blankCells.clearDependents(node)

        notifyNodeChanged(node)

        for (dependentNode in node.clearDependents()) {
            invalidateNode(dependentNode)
        }
    }

    private fun requestEvaluation(node: Node) {
        if (node.attributes.evaluatedValue == EvaluatedValue.Unevaluated) {
            evaluationQueue.put(node)
        }
    }

    private fun cancelEvaluation(node: Node) {
        evaluationQueue.removeIf { it === node }
    }

    private fun evaluateNode(node: Node) {
        evaluateNode(node, mutableSetOf())
    }

    private fun evaluateNode(node: Node, ancestors: MutableSet<Node>): EvaluatedValue? {
        // If a node has already been evaluated we can safely use
        // the value. In case of circular dependencies the node would
        // have been invalidated before, thus would be unevaluated.
        if (node.attributes.evaluatedValue != EvaluatedValue.Unevaluated) {
            return node.attributes.evaluatedValue
        }

        if (node in ancestors) {
            val value = EvaluatedValue.Error(ComputationError.CircularDependency)
            node.update(node.attributes.copy(evaluatedValue = value))
            return value
        }

        // EvaluatedValue.Unevaluated implies that parsedValue was ParsedValue.Formula
        val formula = (node.attributes.parsedValue as ParsedValue.Formula).formula

        establishDependencies(node, formula)

        val dependencies = node.dependencies.map { dependencyNode ->
            ancestors.add(node)
            val result = evaluateNode(dependencyNode, ancestors)
            ancestors.remove(node)
            dependencyNode to result
        }

        val referenceResolver = DependencyReferenceResolver(dependencies)

        val computedValue = formula.compute(referenceResolver)

        val evaluatedValue = computedValue?.toEvaluatedValue()
        node.update(node.attributes.copy(evaluatedValue = evaluatedValue))
        return evaluatedValue
    }

    private fun establishDependencies(node: Node, formula: Formula) {
        for (reference in formula.references) {
            when (reference) {
                is Reference -> {
                    val dependencyNode = grid[reference.cell.x, reference.cell.y]
                    if (dependencyNode != null) {
                        dependencyNode.addDependent(node)
                    } else {
                        blankCells.addDependent(node, reference)
                    }
                }

                is ReferenceRange -> {
                    val dependencyNodes = grid.entries(reference.area)
                    for (dependencyNode in dependencyNodes) {
                        dependencyNode.value.addDependent(node)
                    }

                    blankCells.addDependent(node, reference)
                }
            }
        }
    }

    private class DependencyReferenceResolver(val dependencies: List<Pair<Node, EvaluatedValue?>>) : ReferenceResolver {

        override fun resolve(reference: Reference): ComputedValue? {
            return dependencies.find { (node, _) -> reference.cell.contains(node.column, node.row) }
                ?.let { (_, value) -> value?.toComputedValue() }
        }

        override fun resolve(referenceRange: ReferenceRange): Collection<ComputedValue> {
            return dependencies.asSequence()
                .filter { (node, _) -> referenceRange.area.contains(node.column, node.row) }
                .mapNotNull { (_, value) -> value?.toComputedValue() }
                .toList()
        }
    }

    private fun notifyNodeChanged(node: Node) {
        try {
            changeListener.cellChanged(node.column, node.row, node.attributes)
        } catch (t: Throwable) {
            t.printStackTrace() // TODO
        }
    }

    private fun checkNotClosed() {
        check(!closed.get()) { "spreadsheet engine has been closed" }
    }

    override fun close() {
        closed.set(true)
        evaluationThread.interrupt()
        notifyThread.interrupt()
    }
}

private fun interface ChangeListener {
    fun cellChanged(column: Int, row: Int, attributes: CellAttributes)
}

private data class CellAttributes(
    val value: Value?,
    val parsedValue: ParsedValue?,
    val evaluatedValue: EvaluatedValue?,
) {

    fun isBlank(): Boolean = value == null
    fun isNotBlank(): Boolean = !isBlank()

    companion object {
        val Blank = CellAttributes(null, null, null)
    }
}

private fun EvaluatedValue.toComputedValue(): ComputedValue {
    return when (this) {
        is EvaluatedValue.Text -> ComputedValue.Text(text)
        is EvaluatedValue.Number -> ComputedValue.Number(number)
        is EvaluatedValue.Error -> ComputedValue.Error(error)
        EvaluatedValue.Unevaluated -> error("dependency was not correctly evaluated")
    }
}

private fun ComputedValue.toEvaluatedValue(): EvaluatedValue {
    return when (this) {
        is ComputedValue.Text -> EvaluatedValue.Text(text)
        is ComputedValue.Number -> EvaluatedValue.Number(number)
        is ComputedValue.Reference -> EvaluatedValue.Text(reference.toString())
        is ComputedValue.ReferenceRange -> EvaluatedValue.Text(referenceRange.toString())
        is ComputedValue.Error -> EvaluatedValue.Error(error)
    }
}
