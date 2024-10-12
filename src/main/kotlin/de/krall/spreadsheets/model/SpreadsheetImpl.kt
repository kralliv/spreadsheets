package de.krall.spreadsheets.model

import de.krall.spreadsheets.grid.SparseGrid
import de.krall.spreadsheets.util.empty
import de.krall.spreadsheets.value.EvaluatedValue
import de.krall.spreadsheets.value.ParsedValue
import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import java.io.Closeable
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

class SpreadsheetImpl : Spreadsheet {


    override fun get(row: Int, column: Int): Cell {
        TODO()
    }


    override fun addListener(listener: SpreadsheetListener) {}
    override fun removeListener(listener: SpreadsheetListener) {}
}

private class SpreadsheetEngine(
    private val valueParser: ValueParser,
    private val lazyEvaluation: Boolean,
    private val changeListener: ChangeListener,
) : Closeable {

    private val closed = AtomicBoolean(false)
    private val lock = ReentrantReadWriteLock()

    private val grid = SparseGrid<Node>()
    private val blankCellDependencies = BlankCellDependencies()

    private val evaluationQueue = LinkedBlockingDeque<Node>()
    private val evaluationThread = thread(name = "spreadsheet-engine-evaluation") {
        while (!closed.get()) {
            try {
                val node = evaluationQueue.take()

                evaluateNode(node)
            } catch (t: Throwable) {
                t.printStackTrace() // TODO
            }
        }
    }

    private class Node(
        val row: Int,
        val column: Int,
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

        val dependentNodes = mutableListOf<Node>()
    }

    fun read(row: Int, column: Int): CellAttributes = lock.read {
        checkNotClosed()

        val node = grid[row, column]
        if (node != null && lazyEvaluation) {
            requestEvaluation(node)
        }
        return node?.attributes ?: CellAttributes.Blank
    }

    fun write(row: Int, column: Int, mutation: (CellAttributes) -> CellAttributes) = lock.write {
        checkNotClosed()

        var node = grid[row, column]

        val previousAttributes = node?.attributes ?: CellAttributes.Blank
        val mutatedAttributes = mutation(previousAttributes)
        val attributes = adjustAttributes(mutatedAttributes, previousAttributes)

        if (attributes.isNotBlank()) {
            if (node == null) {
                insert(row, column, attributes)
            } else {
                update(node, attributes)
            }
        } else if (node != null) {
            remove(node)
        }
    }

    private fun adjustAttributes(attributes: CellAttributes, previousAttributes: CellAttributes): CellAttributes {
        var updatedAttributes = parseValue(attributes, previousAttributes)
        updatedAttributes = fastEvaluateValue(attributes, previousAttributes)
        return updatedAttributes
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

    private fun fastEvaluateValue(attributes: CellAttributes, previousAttributes: CellAttributes): CellAttributes {
        val valueUnchanged = attributes.parsedValue === previousAttributes.parsedValue
        val nullnessMatches = (attributes.parsedValue == null) == (attributes.evaluatedValue == null)
        if (valueUnchanged && nullnessMatches) return attributes

        val evaluatedValue = when (val value = attributes.parsedValue) {
            null -> null
            is ParsedValue.Text -> EvaluatedValue.Text(value.text)
            is ParsedValue.Number -> EvaluatedValue.Number(value.number)
            is ParsedValue.Formula -> EvaluatedValue.Unevaluated
            is ParsedValue.BadFormula -> EvaluatedValue.BadFormula
        }

        return attributes.copy(evaluatedValue = evaluatedValue)
    }

    private fun insert(row: Int, column: Int, attributes: CellAttributes): Node {
        val node = Node(row, column, attributes)
        node.dependentNodes.addAll(blankCellDependencies.take(row, column))
        grid[row, column] = node

        notifyNodeChanged(node)

        invalidateNodeDependencies(node)

        if (!lazyEvaluation) {
            requestEvaluation(node)
        }

        return node
    }

    private fun update(node: Node, attributes: CellAttributes) {
        val previousAttributes = node.update(attributes)

        notifyNodeChanged(node)

        if (previousAttributes.parsedValue !== attributes.parsedValue) {
            invalidateNodeDependencies(node)

            // TODO retract dependencies

            if (!lazyEvaluation) {
                requestEvaluation(node)
            }
        }
    }

    private fun remove(node: Node) {
        grid[node.row, node.column] = null
        node.update(CellAttributes.Blank)

        notifyNodeChanged(node)

        invalidateNodeDependencies(node)
    }

    private fun invalidateNodeDependencies(node: Node) {
        val queue = ArrayDeque<Node>()
        queue.addAll(node.dependentNodes.empty())

        while (queue.isNotEmpty()) {
            val dependentNode = queue.removeFirst()

            var attributes = dependentNode.attributes
            if (attributes.evaluatedValue != null) {
                attributes = attributes.copy(evaluatedValue = EvaluatedValue.Unevaluated)
                dependentNode.update(attributes)
            }

            notifyNodeChanged(node)

            queue.addAll(dependentNode.dependentNodes.empty())
        }
    }

    private fun requestEvaluation(node: Node) {
        if (node.attributes.evaluatedValue == EvaluatedValue.Unevaluated) {
            evaluationQueue.put(node)
        }
    }

    private fun evaluateNode(node: Node) {
        val queue = ArrayDeque<Node>()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()

        }
    }

    private fun notifyNodeChanged(node: Node) {
        try {

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
    }

    private class BlankCellDependencies {

        fun take(row: Int, column: Int): List<Node> {
            TODO()
        }
    }
}

private fun interface ChangeListener {
    fun cellChanged(row: Int, column: Int, attributes: CellAttributes)
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

