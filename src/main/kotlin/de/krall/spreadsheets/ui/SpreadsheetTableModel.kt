package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.value.Value
import javax.swing.table.AbstractTableModel

class SpreadsheetTableModel(val spreadsheet: Spreadsheet) : AbstractTableModel() {

    override fun getRowCount(): Int = 100
    override fun getColumnCount(): Int = 100

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
        return spreadsheet[columnIndex, rowIndex].value
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        spreadsheet[columnIndex, rowIndex].value = value as Value?
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }
}