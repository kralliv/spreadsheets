package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.value.Value
import javax.swing.table.AbstractTableModel

class SpreadsheetTableModel(val spreadsheet: Spreadsheet) : AbstractTableModel() {

    override fun getRowCount(): Int = 100
    override fun getColumnCount(): Int = 100

    override fun getColumnName(column: Int): String {
        if (column == 0) return ""
        return buildString {
            var c = column - 1
            while (c >= 0) {
                append('A' + (c % 26))

                c = c / 26 - 1
            }
            reverse()
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex == 0) {
            (rowIndex + 1).toString()
        } else {
            spreadsheet[columnIndex - 1, rowIndex].value
        }
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) return

        spreadsheet[columnIndex - 1, rowIndex].value = value as Value?
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*>? {
        if (columnIndex == 0) return String::class.java
        return Value::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex != 0
    }
}