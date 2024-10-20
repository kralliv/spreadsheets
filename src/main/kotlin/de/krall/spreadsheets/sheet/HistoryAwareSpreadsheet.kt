package de.krall.spreadsheets.sheet

import de.krall.spreadsheets.util.empty
import de.krall.spreadsheets.value.EvaluatedValue
import de.krall.spreadsheets.value.Value
import java.util.concurrent.CopyOnWriteArrayList

class HistoryAwareSpreadsheet(private val delegate: Spreadsheet) : Spreadsheet {

    private val changes = mutableListOf<Change>()
    private var index = -1

    private var accumulate = false
    private val uncommittedChanges = mutableListOf<Change>()

    fun canUndo(): Boolean = index >= 0

    fun undo() {
        if (!canUndo()) return

        changes[index].undo(delegate)
        index--
    }

    fun canRedo(): Boolean = index < changes.lastIndex

    fun redo() {
        if (!canRedo()) return

        index++
        changes[index].redo(delegate)
    }

    fun compositeChange(block: () -> Unit) {
        accumulate = true
        try {
            block()
        } finally {
            accumulate = false

            if (uncommittedChanges.isNotEmpty()) {
                commitChange(CompositeChange(uncommittedChanges.empty()))
            }
        }
    }

    private fun recordChange(change: Change) {
        if (accumulate) {
            uncommittedChanges.add(change)
        } else {
            commitChange(change)
        }
    }

    private fun commitChange(change: Change) {
        if (changes.lastIndex > index) {
            changes.subList(index + 1, changes.size).clear()
        }

        changes.add(change)
        index++
    }

    private val listeners = CopyOnWriteArrayList<SpreadsheetListener>()

    private val delegateListener = object : SpreadsheetListener {
        override fun cellChanged(cell: Cell, previousCell: Cell) {
            val wrappedCell = CellWrapper(cell)
            listeners.forEach { it.cellChanged(wrappedCell, previousCell) }
        }

        override fun cellUpdated(cell: Cell) {
            val wrappedCell = CellWrapper(cell)
            listeners.forEach { it.cellUpdated(wrappedCell) }
        }
    }

    init {
        delegate.addListener(delegateListener)
    }

    override fun get(row: Int, column: Int): Cell {
        return CellWrapper(delegate[row, column])
    }

    override val rows: Sequence<Row>
        get() = delegate.rows.map { RowWrapper(it) }


    override fun addListener(listener: SpreadsheetListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SpreadsheetListener) {
        listeners.remove(listener)
    }

    override fun close() {
        changes.clear()
        index = -1

        delegate.removeListener(delegateListener)
        delegate.close()
    }

    private inner class RowWrapper(val delegate: Row) : Row {

        override val row: Int
            get() = delegate.row

        override fun get(column: Int): Cell {
            return CellWrapper(delegate[column])
        }

        override val cells: Sequence<Cell>
            get() = delegate.cells.map { CellWrapper(it) }
    }

    private inner class CellWrapper(val delegate: Cell) : Cell {

        override val row: Int
            get() = delegate.row
        override val column: Int
            get() = delegate.column

        override var value: Value?
            get() = delegate.value
            set(value) {
                val previousValue = delegate.value
                delegate.value = value
                recordChange(ValueChange(row, column, value, previousValue))
            }
        override val evaluatedValue: EvaluatedValue?
            get() = delegate.evaluatedValue
    }
}

private interface Change {
    fun undo(spreadsheet: Spreadsheet)
    fun redo(spreadsheet: Spreadsheet)
}

private class CompositeChange(
    val changes: List<Change>,
) : Change {

    override fun undo(spreadsheet: Spreadsheet) {
        changes.forEach { it.undo(spreadsheet) }
    }

    override fun redo(spreadsheet: Spreadsheet) {
        changes.forEach { it.redo(spreadsheet) }
    }
}

private class ValueChange(
    val row: Int,
    val column: Int,
    val value: Value?,
    val previousValue: Value?,
) : Change {

    override fun undo(spreadsheet: Spreadsheet) {
        spreadsheet[row, column].value = previousValue
    }

    override fun redo(spreadsheet: Spreadsheet) {
        spreadsheet[row, column].value = value
    }
}
