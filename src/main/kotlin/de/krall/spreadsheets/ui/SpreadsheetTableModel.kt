package de.krall.spreadsheets.ui

import javax.swing.table.AbstractTableModel

class SpreadsheetTableModel(val model: SpreadsheetModel) : AbstractTableModel() {

    override fun getRowCount(): Int = 100
    override fun getColumnCount(): Int = 100

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex == 0) {
            (rowIndex + 1).toString()
        } else {
            model.getValue(rowIndex, columnIndex - 1)
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) return

        model.getValue(rowIndex, columnIndex - 1)
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*>? {
        return String::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex != 0
    }
}