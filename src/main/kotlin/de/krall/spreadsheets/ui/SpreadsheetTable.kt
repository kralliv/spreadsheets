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
import de.krall.spreadsheets.ui.components.table.HeaderCellRenderer
import de.krall.spreadsheets.ui.components.table.RowHeaderCellRenderer
import de.krall.spreadsheets.ui.components.table.STable
import de.krall.spreadsheets.ui.util.invokeLater
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import javax.swing.JComponent
import javax.swing.TransferHandler
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableColumn

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
        transferHandler = SpreadsheetTransferHandler()

        tableHeader?.defaultRenderer = HeaderCellRenderer()
        tableRowHeader?.defaultRenderer = RowHeaderCellRenderer()

        isSortable = true

        setDefaultRenderer(Value::class.java, ValueCellRenderer(this.spreadsheet))
        setDefaultEditor(Value::class.java, ValueCellEditor(parser))

        installKeyboardActions()
        installRepaintHandling()
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

    override fun initializeColumnPreferredWidth(column: TableColumn) {
        column.preferredWidth = 100
    }

    private fun installKeyboardActions() {
        registerKeyboardAction(KeyStroke("BACK_SPACE"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            val model = model
            spreadsheet.compositeChange {
                selectedRows.forEach { row ->
                    selectedColumns.forEach { column ->
                        model.setValueAt(null, row, column)
                    }
                }
            }
        }
    }

    private fun installRepaintHandling() {
        spreadsheet.addListener(object : SpreadsheetListener {
            override fun cellChanged(cell: Cell, previousCell: Cell) {
                // Cell will also be updated
            }

            override fun cellUpdated(cell: Cell) {
                invokeLater { repaint() }
            }
        })

        columnModel.selectionModel.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent) {
                val tableHeader = tableHeader

                val first = tableHeader.getHeaderRect(e.firstIndex)
                val last = tableHeader.getHeaderRect(e.lastIndex)

                tableHeader.repaint(first.union(last))
            }
        })
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
