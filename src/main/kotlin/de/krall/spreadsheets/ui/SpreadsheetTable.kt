package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Cell
import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.model.SpreadsheetListener
import de.krall.spreadsheets.ui.event.invokeLater
import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import org.jdesktop.swingx.JXTable
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener

class SpreadsheetTable(val spreadsheet: Spreadsheet, val parser: ValueParser) : JXTable() {

    init {
        isHorizontalScrollEnabled = true
        columnSelectionAllowed = true
        rowSelectionAllowed = true

        tableHeader.reorderingAllowed = false

        model = SpreadsheetTableModel(spreadsheet)

        spreadsheet.addListener(object : SpreadsheetListener {
            override fun cellChanged(cell: Cell) {
                invokeLater { repaint() }
            }
        })

        tableHeader.defaultRenderer = HeaderCellRenderer()
        getColumn(0).apply {
            cellRenderer = HeaderCellRenderer()
            preferredWidth = 60
        }

        for (index in 1..<model.columnCount) {
            getColumn(index).preferredWidth = 100
        }

        setDefaultRenderer(Value::class.java, ValueCellRenderer(spreadsheet))
        setDefaultEditor(Value::class.java, ValueCellEditor(parser))

        columnModel.selectionModel.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent) {
                val tableHeader = tableHeader

                val first = tableHeader.getHeaderRect(e.firstIndex)
                val last = tableHeader.getHeaderRect(e.lastIndex)

                tableHeader.repaint(first.union(last))
            }
        })
    }
}
