package de.krall.spreadsheets.ui.components.table

import de.krall.spreadsheets.ui.event.isLeftButton
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyChangeListener
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.JTable
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel

class STableRowHeader : JList<Any>() {

    private val propertyChangeListener = PropertyChangeListener { event ->
        when (event.propertyName) {
            "model" -> updateTableModel()
            "selectionModel" -> updateSelectionModel()
            "rowHeight" -> updateRowHeight()
        }
    }

    private val tableModelListener = TableModelListener {
        updateRowCount()
    }

    private val tableSelectionModelListener = ListSelectionListener { event ->
        updateSelection()
    }

    var table: JTable? = null
        set(table) {
            val previousTable = field
            if (table !== previousTable) {
                previousTable?.removePropertyChangeListener(propertyChangeListener)

                field = table

                table?.addPropertyChangeListener(propertyChangeListener)

                updateTableModel()
                updateSelectionModel()
                updateRowHeight()
            }
        }

    var tableModel: TableModel? = null
        private set(model) {
            val previousModel = field
            if (model !== previousModel) {
                previousModel?.removeTableModelListener(tableModelListener)

                field = model

                model?.addTableModelListener(tableModelListener)

                updateRowCount()
            }
        }

    private var selectionChanging = false
    var tableSelectionModel: ListSelectionModel? = null
        private set(tableSelectionModel) {
            val previousTableSelectionModel = field
            if (tableSelectionModel !== previousTableSelectionModel) {
                previousTableSelectionModel?.removeListSelectionListener(tableSelectionModelListener)

                field = tableSelectionModel

                tableSelectionModel?.addListSelectionListener(tableSelectionModelListener)

                updateSelection()
            }
        }

    var defaultRenderer: TableCellRenderer = DefaultTableCellRenderer()
        set(renderer) {
            cellRenderer = TableCellRendererAdapter(renderer)
        }

    init {
        isFocusable = false

        cellRenderer = TableCellRendererAdapter(defaultRenderer)
        fixedCellHeight = 22
        fixedCellWidth = 60
        model = RowListModel(0)

        val mouseHandler = object : MouseAdapter() {
            private var startIndex = -1

            override fun mousePressed(e: MouseEvent) {
                if (!e.isLeftButton) return

                startIndex = locationToIndex(e.point)

                performSelection(e)
            }

            override fun mouseDragged(e: MouseEvent) {
                if (!e.isLeftButton) return

                performSelection(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (!e.isLeftButton) return

                startIndex = -1
            }

            private fun performSelection(e: MouseEvent) {
                table?.let { table ->
                    val rowSelectionModel = table.selectionModel

                    val endIndex = locationToIndex(e.point)
                    rowSelectionModel.setSelectionInterval(startIndex, endIndex)
                    selectionModel.setSelectionInterval(startIndex, endIndex)

                    val columnSelectionModel = table.columnModel.selectionModel

                    columnSelectionModel.addSelectionInterval(table.columnCount, 0)
                }
            }
        }

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
    }

    private fun updateTableModel() {
        tableModel = table?.model
    }

    private fun updateRowCount() {
        model = RowListModel(tableModel?.rowCount ?: 0)
    }

    private fun updateSelectionModel() {
        tableSelectionModel = table?.selectionModel
    }

    private fun updateSelection() {
        withSelectionChanging {
            val tableSelectionModel = tableSelectionModel
            if (tableSelectionModel != null && tableSelectionModel.selectionMode != selectionModel.selectionMode) {
                selectionModel.selectionMode = tableSelectionModel.selectionMode
            }

            if (tableSelectionModel != null) {
                tableSelectionModel.copyTo(selectionModel)
            } else {
                selectionModel.clearSelection()
            }
        }
    }

    private inline fun withSelectionChanging(block: () -> Unit) {
        if (selectionChanging) return
        selectionChanging = true
        try {
            block()
        } finally {
            selectionChanging = false
        }
    }

    private fun ListSelectionModel.copyTo(other: ListSelectionModel) {
        other.clearSelection()

        selectedIndices.forEach { index ->
            other.addSelectionInterval(index, index)
        }
    }

    private fun updateRowHeight() {
        fixedCellHeight = table?.rowHeight ?: 22
    }

    private inner class TableCellRendererAdapter(private val tableCellRenderer: TableCellRenderer) : ListCellRenderer<Any> {

        override fun getListCellRendererComponent(
            list: JList<out Any>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            hasFocus: Boolean,
        ): Component {
            return tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, index, -1)
        }
    }

    private class RowListModel(private val size: Int) : AbstractListModel<Any>() {

        override fun getSize(): Int = size

        override fun getElementAt(index: Int): Any {
            return index + 1
        }
    }
}
