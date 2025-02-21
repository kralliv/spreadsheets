package de.krall.spreadsheets.sheet

import de.krall.spreadsheets.sheet.grid.SparseGrid
import de.krall.spreadsheets.sheet.value.ComputationError
import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.EvaluatedValue
import de.krall.spreadsheets.sheet.value.ParsedValue
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange
import de.krall.spreadsheets.sheet.value.Referencing
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.util.empty
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.asSequence
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

class SpreadsheetImpl(val parser: ValueParser) : Spreadsheet {

    private val listeners = CopyOnWriteArrayList<SpreadsheetListener>()

    private val engine = SpreadsheetEngine(parser, lazyEvaluation = true) { notification ->
        when (notification) {
            is Notification.CellChanged -> {
                val cell = LiveCell(notification.column, notification.row)
                val previousCell = SnapshotCell(notification.column, notification.row, notification.previousAttributes)

                listeners.forEach { it.cellChanged(cell, previousCell) }
            }

            is Notification.CellUpdated -> {
                val cell = LiveCell(notification.column, notification.row)

                listeners.forEach { it.cellUpdated(cell) }
            }
        }
    }

    override fun get(row: Int, column: Int): Cell {
        return LiveCell(row, column)
    }

    override val rows: Sequence<Row>
        get() = engine.rows().asSequence().map { row -> LiveRow(row) }

    private inner class LiveRow(
        override val row: Int,
    ) : Row {

        override fun get(column: Int): Cell = get(row, column)

        override val cells: Sequence<Cell>
            get() = engine.columns(row).asSequence().map { column -> get(row, column) }
    }

    private inner class LiveCell(
        override val row: Int,
        override val column: Int,
    ) : Cell {

        override var value: Value?
            get() = read().value
            set(value) {
                write { it.copy(value = value) }
            }

        override val evaluatedValue: EvaluatedValue?
            get() = read().evaluatedValue

        private fun read(): CellAttributes {
            return engine.read(column, row)
        }

        private fun write(mutation: (CellAttributes) -> CellAttributes) {
            engine.write(column, row, mutation)
        }
    }

    private class SnapshotCell(
        override val row: Int,
        override val column: Int,
        val attributes: CellAttributes,
    ) : Cell {

        override var value: Value?
            get() = attributes.value
            set(value) {}

        override val evaluatedValue: EvaluatedValue?
            get() = attributes.evaluatedValue
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

// The engine is column-major to simplify interaction with
// a x/y grid. The rest of the application is row-major.
private class SpreadsheetEngine(
    private val valueParser: ValueParser,
    private val lazyEvaluation: Boolean,
    private val changeListener: NotificationListener,
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

        fun takeDependents(column: Int, row: Int): List<Node> {
            val dependencies = mutableListOf<Node>()
            val iterator = references.iterator()
            while (iterator.hasNext()) {
                val (node, referencing) = iterator.next()

                when (referencing) {
                    is Reference -> {
                        if (referencing.cell.contains(column, row)) {
                            iterator.remove()
                            dependencies.add(node)
                        }
                    }

                    is ReferenceRange -> {
                        if (referencing.area.contains(column, row)) {
                            // areas don't get removed here as there might
                            // be other blanks cell covered.
                            dependencies.add(node)
                        }
                    }
                }
            }
            return dependencies
        }
    }

    private val closed = AtomicBoolean(false)
    private val writeRequest = AtomicReference<CountDownLatch>()
    private val lock = ReentrantReadWriteLock()

    private val grid = SparseGrid<Node>()
    private val blankCells = BlankCells()

    private val evaluationQueue = LinkedBlockingDeque<Node>()
    private val evaluationThread = thread(name = "spreadsheet-engine-evaluation") {
        while (!closed.get()) {
            try {
                val node = evaluationQueue.take()

                val reschedule = { requestEvaluation(node) }
                work(reschedule) {
                    evaluateNode(node)
                }
            } catch (ignore: InterruptedException) {
            } catch (t: Throwable) {
                LOG.error(t) { "evaluation resulted in exception" }
            }
        }
    }

    private val notifyQueue = LinkedBlockingDeque<Notification>()
    private val notifyThread = thread(name = "spreadsheet-engine-notify") {
        while (!closed.get()) {
            try {
                val notification = notifyQueue.take()

                changeListener.notify(notification)
            } catch (ignore: InterruptedException) {
            } catch (t: Throwable) {
                LOG.error(t) { "notification resulted in exception" }
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

    fun write(column: Int, row: Int, mutation: (CellAttributes) -> CellAttributes) = write {
        checkNotClosed()

        val node = grid[column, row]

        val previousAttributes = node?.attributes ?: CellAttributes.Blank
        val mutatedAttributes = mutation(previousAttributes)
        val attributes = parseValue(mutatedAttributes, previousAttributes)

        if (!attributes.hasNonVolatileDifferences(previousAttributes)) return@write

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
        grid[column, row] = node

        for (dependentNode in blankCells.takeDependents(column, row)) {
            node.addDependent(dependentNode)
        }

        notifyNodeChanged(node, CellAttributes.Blank)

        invalidateNode(node)

        if (!lazyEvaluation) {
            requestEvaluation(node)
        }

        return node
    }

    private fun update(node: Node, attributes: CellAttributes) {
        val previousAttributes = node.update(attributes)

        notifyNodeChanged(node, previousAttributes)

        if (previousAttributes.parsedValue !== attributes.parsedValue) {
            invalidateNode(node)

            if (!lazyEvaluation) {
                requestEvaluation(node)
            }
        }
    }

    private fun remove(node: Node) {
        grid[node.column, node.row] = null
        val previousAttributes = node.update(CellAttributes.Blank)

        notifyNodeChanged(node, previousAttributes)

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

        notifyNodeUpdated(node)

        for (dependentNode in node.clearDependents()) {
            invalidateNode(dependentNode)
        }
    }

    private fun requestEvaluation(node: Node) {
        if (node.attributes.evaluatedValue == EvaluatedValue.Unevaluated && node !in evaluationQueue) {
            evaluationQueue.put(node)
        }
    }

    private fun cancelEvaluation(node: Node) {
        evaluationQueue.removeIf { it === node }
    }

    private fun evaluateNode(node: Node) {
        evaluateNode(node, emptySet())
    }

    private fun evaluateNode(node: Node, ancestors: Set<Node>): EvaluatedValue? {
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

        val referenceResolver = DynamicReferenceResolver(node, ancestors + node)

        // Last chance to cancel the evaluation on the level
        if (isWriteRequested()) return EvaluatedValue.Error(ComputationError.Cancelled)

        val computedValue = try {
            formula.compute(referenceResolver)
        } catch (throwable: Throwable) {
            LOG.error(throwable) { "evaluation resulted in exception: ${node.attributes.value}" }
            ComputedValue.Error(ComputationError.Error)
        }

        val evaluatedValue = computedValue.toEvaluatedValue()
        node.update(node.attributes.copy(evaluatedValue = evaluatedValue))

        notifyNodeUpdated(node)

        return evaluatedValue
    }

    private inner class DynamicReferenceResolver(
        private val node: Node,
        private val ancestors: Set<Node>,
    ) : ReferenceResolver {

        override fun resolve(reference: Reference): ComputedValue {
            val dependencyNode = grid[reference.cell.x, reference.cell.y]
            if (dependencyNode != null) {
                dependencyNode.addDependent(node)
            } else {
                blankCells.addDependent(node, reference)
            }

            return dependencyNode
                ?.let { evaluateNode(dependencyNode, ancestors)?.toComputedValue() }
                ?: ComputedValue.Blank
        }

        override fun resolve(referenceRange: ReferenceRange): Collection<ComputedValue> {
            val dependencyNodes = grid.entries(referenceRange.area)
            for (dependencyNode in dependencyNodes) {
                dependencyNode.value.addDependent(node)
            }
            blankCells.addDependent(node, referenceRange)

            return dependencyNodes
                .mapNotNull { dependencyNode -> evaluateNode(dependencyNode.value, ancestors)?.toComputedValue() }
                .toList()
        }
    }

    private fun notifyNodeChanged(node: Node, previousAttributes: CellAttributes) {
        notifyQueue.add(Notification.CellChanged(node.column, node.row, node.attributes, previousAttributes))
    }

    private fun notifyNodeUpdated(node: Node) {
        notifyQueue.add(Notification.CellUpdated(node.column, node.row, node.attributes))
    }

    private fun checkNotClosed() {
        check(!closed.get()) { "spreadsheet engine has been closed" }
    }

    fun rows(): List<Int> = lock.read {
        grid.entries.map { it.y }.toSortedSet().toList()
    }

    fun columns(row: Int): List<Int> = lock.read {
        grid.entries.filter { it.y == row }.map { it.x }.toSortedSet().toList()
    }

    override fun close() {
        closed.set(true)
        evaluationThread.interrupt()
        notifyThread.interrupt()
    }

    private inline fun work(yield: () -> Unit, block: () -> Unit) {
        awaitWriteRequest()
        lock.read {
            try {
                block()
            } catch (_: WriteRequestedException) {
                yield()
            }
        }
    }

    private inline fun write(block: () -> Unit) {
        requestWrite()
        lock.write {
            clearWriteRequest()

            block()
        }
    }

    private fun requestWrite() {
        writeRequest.compareAndSet(null, CountDownLatch(1))
    }

    private fun clearWriteRequest() {
        writeRequest.getAndSet(null)?.countDown()
    }

    private fun checkWriteRequest() {
        if (writeRequest.get() != null) {
            throw WriteRequestedException()
        }
    }

    private fun isWriteRequested(): Boolean {
        return writeRequest.get() != null
    }

    private fun awaitWriteRequest() {
        writeRequest.get()?.await()
    }

    private class WriteRequestedException : RuntimeException()

    companion object {
        private val LOG = KotlinLogging.logger { }
    }
}

private fun interface NotificationListener {
    fun notify(notification: Notification)
}

private sealed class Notification {
    data class CellChanged(
        val column: Int,
        val row: Int,
        val attributes: CellAttributes,
        val previousAttributes: CellAttributes,
    ) : Notification()

    data class CellUpdated(
        val column: Int,
        val row: Int,
        val attributes: CellAttributes,
    ) : Notification()
}

private data class CellAttributes(
    val value: Value?,
    val parsedValue: ParsedValue?,
    val evaluatedValue: EvaluatedValue?,
) {

    fun isBlank(): Boolean = value == null
    fun isNotBlank(): Boolean = !isBlank()

    fun hasNonVolatileDifferences(other: CellAttributes): Boolean {
        return value != other.value
    }

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

private fun ComputedValue.toEvaluatedValue(): EvaluatedValue? {
    return when (this) {
        is ComputedValue.Blank -> null
        is ComputedValue.Text -> EvaluatedValue.Text(text)
        is ComputedValue.Number -> EvaluatedValue.Number(number)
        is ComputedValue.Reference -> EvaluatedValue.Text(reference.toString())
        is ComputedValue.ReferenceRange -> EvaluatedValue.Text(referenceRange.toString())
        is ComputedValue.Error -> EvaluatedValue.Error(error)
    }
}
