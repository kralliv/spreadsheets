package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.value.Value
import javax.swing.table.AbstractTableModel
import kotlin.math.max

class SpreadsheetTableModel(val spreadsheet: Spreadsheet) : AbstractTableModel() {

    private var rowCount: Int
    private var columnCount: Int

    init {
        var maxRow = 0
        var maxColumn = 0
        for (row in spreadsheet.rows) {
            maxRow = max(row.row, maxRow)
            for (cell in row.cells) {
                maxColumn = max(cell.column, maxColumn)
            }
        }
        rowCount = (maxRow + 10).coerceAtLeast(100)
        columnCount = (maxColumn + 5).coerceAtLeast(26)
    }

    override fun getRowCount(): Int = rowCount
    override fun getColumnCount(): Int = columnCount

    fun addRows(count: Int) {
        val previousRowCount = rowCount
        rowCount += count
        fireTableRowsInserted(previousRowCount, rowCount - 1)
    }

    fun addColumns(count: Int) {
        columnCount += count
        fireTableStructureChanged()
    }

    override fun getColumnName(column: Int): String {
        return buildString {
            var c = column
            while (c >= 0) {
                append('A' + (c % 26))

                c = c / 26 - 1
            }
            reverse()
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*>? {
        return Value::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return spreadsheet[rowIndex, columnIndex].value
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        spreadsheet[rowIndex, columnIndex].value = value as Value?
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }
}