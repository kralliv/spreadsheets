package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.Cell
import de.krall.spreadsheets.sheet.HistoryAwareSpreadsheet
import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.sheet.SpreadsheetListener
import de.krall.spreadsheets.sheet.transfer.SpreadsheetTransferable
import de.krall.spreadsheets.sheet.transfer.TransferableCell
import de.krall.spreadsheets.sheet.transfer.TransferableSpreadsheet
import de.krall.spreadsheets.sheet.transfer.fromCsv
import de.krall.spreadsheets.ui.render.renderToString
import de.krall.spreadsheets.ui.table.HeaderCellRenderer
import de.krall.spreadsheets.ui.table.RowHeaderCellRenderer
import de.krall.spreadsheets.ui.table.STable
import de.krall.spreadsheets.ui.util.invokeLater
import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import javax.swing.JComponent
import javax.swing.TransferHandler
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SpreadsheetTable(spreadsheet: Spreadsheet, val parser: ValueParser) : STable() {

    val spreadsheet = HistoryAwareSpreadsheet(spreadsheet)

    private val spreadsheetModel: SpreadsheetTableModel
        get() = model as SpreadsheetTableModel

    init {
        isHorizontalScrollEnabled = true
        columnSelectionAllowed = true
        rowSelectionAllowed = true

        tableHeader.reorderingAllowed = false

        model = SpreadsheetTableModel(this.spreadsheet)

        this.spreadsheet.addListener(object : SpreadsheetListener {
            override fun cellChanged(cell: Cell, previousCell: Cell) {
                // Cell will also be updated
            }

            override fun cellUpdated(cell: Cell) {
                invokeLater { repaint() }
            }
        })

        tableHeader?.defaultRenderer = HeaderCellRenderer()
        tableRowHeader?.defaultRenderer = RowHeaderCellRenderer()

        for (index in 0..<model.columnCount) {
            getColumn(index).preferredWidth = 100
        }

        setDefaultRenderer(Value::class.java, ValueCellRenderer(this.spreadsheet))
        setDefaultEditor(Value::class.java, ValueCellEditor(parser))

        columnModel.selectionModel.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent) {
                val tableHeader = tableHeader

                val first = tableHeader.getHeaderRect(e.firstIndex)
                val last = tableHeader.getHeaderRect(e.lastIndex)

                tableHeader.repaint(first.union(last))
            }
        })

        transferHandler = SpreadsheetTransferHandler()
    }

    fun undo() {
        spreadsheet.undo()
    }

    fun redo() {
        spreadsheet.redo()
    }

    fun addRows(count: Int) {
        spreadsheetModel.addRows(count)
    }

    fun addColumns(count: Int) {
        spreadsheetModel.addColumns(count)
    }

    private inner class SpreadsheetTransferHandler : TransferHandler() {

        override fun exportToClipboard(comp: JComponent, clip: Clipboard, action: Int) {
            val selectedRows = selectedRows
            val selectedColumns = selectedColumns

            if (selectedRows.isEmpty() || selectedColumns.isEmpty()) return

            val renderer = getDefaultRenderer(Value::class.java)

            val rows = selectedRows.map { row ->
                selectedColumns.map { column ->
                    val cell = spreadsheet[row, column]

                    val value = cell.value
                    val displayableValue = renderer.renderToString(this@SpreadsheetTable, value, false, false, row, column)

                    if (action == MOVE) {
                        cell.value = null
                    }

                    TransferableCell(
                        value,
                        displayableValue,
                    )
                }
            }

            val data = TransferableSpreadsheet.fromRows(rows)
            val transferable = SpreadsheetTransferable(data)

            clip.setContents(transferable, transferable)
        }

        override fun canImport(support: TransferSupport): Boolean {
            return support.isDataFlavorSupported(SpreadsheetTransferable.Flavor)
                    || support.isDataFlavorSupported(DataFlavor.stringFlavor)
        }

        override fun importData(support: TransferSupport): Boolean {
            val selectedRows = selectedRows
            val selectedColumns = selectedColumns

            if (selectedRows.isEmpty() || selectedColumns.isEmpty()) return false

            val data = when {
                support.isDataFlavorSupported(SpreadsheetTransferable.Flavor) -> {
                    support.transferable.getTransferData(SpreadsheetTransferable.Flavor) as TransferableSpreadsheet
                }

                support.isDataFlavorSupported(DataFlavor.stringFlavor) -> {
                    val csv = support.transferable.getTransferData(DataFlavor.stringFlavor) as String
                    TransferableSpreadsheet.fromCsv(csv)
                }

                else -> return false
            }

            spreadsheet.compositeChange {
                var rowOffset = 0
                for (row in selectedRows) {
                    var columnOffset = 0
                    for (column in selectedColumns) {
                        val cell = spreadsheet[row, column]

                        cell.value = data[rowOffset % data.rowCount, columnOffset % data.columnCount].value

                        columnOffset++
                    }
                    rowOffset++
                }
            }

            return true
        }
    }
}
