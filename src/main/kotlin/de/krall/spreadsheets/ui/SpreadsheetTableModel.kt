package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.value.Value
import javax.swing.table.AbstractTableModel

class SpreadsheetTableModel(val model: Spreadsheet) : AbstractTableModel() {

    override fun getRowCount(): Int = 100
    override fun getColumnCount(): Int = 100

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex == 0) {
            (rowIndex + 1).toString()
        } else {
            model[rowIndex, columnIndex - 1].value
        }
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) return

        model[rowIndex, columnIndex - 1].value = value as Value?
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*>? {
        return String::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex != 0
    }
}