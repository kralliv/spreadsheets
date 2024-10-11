package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import org.jdesktop.swingx.JXTable

class SpreadsheetTable(val spreadsheet: Spreadsheet) : JXTable() {

    init {
        isHorizontalScrollEnabled = true
        border = null

        model = SpreadsheetTableModel(spreadsheet)

        setDefaultRenderer(String::class.java, ValueCellRenderer(spreadsheet))
        setDefaultEditor(String::class.java, ValueCellEditor())
    }
}
