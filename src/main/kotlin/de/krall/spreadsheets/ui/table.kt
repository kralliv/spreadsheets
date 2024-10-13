package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Cell
import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.model.SpreadsheetListener
import de.krall.spreadsheets.util.invokeLater
import de.krall.spreadsheets.value.parser.ValueParser
import org.jdesktop.swingx.JXTable

class SpreadsheetTable(val spreadsheet: Spreadsheet, val parser: ValueParser) : JXTable() {

    init {
        isHorizontalScrollEnabled = true
        border = null

        model = SpreadsheetTableModel(spreadsheet)

        spreadsheet.addListener(object : SpreadsheetListener {
            override fun cellChanged(cell: Cell) {
                invokeLater { repaint() }
            }
        })

        setDefaultRenderer(String::class.java, ValueCellRenderer(spreadsheet))
        setDefaultEditor(String::class.java, ValueCellEditor(parser))
    }
}
