package de.krall.spreadsheets.ui.table

import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import org.jdesktop.swingx.JXTable
import java.awt.event.ActionListener
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.SwingUtilities
import javax.swing.plaf.TableUI
import javax.swing.plaf.UIResource

open class STable : JXTable() {

    var tableRowHeader: STableRowHeader? = null
        set(tableRowHeader) {
            val previousTableRowHeader = field
            if (tableRowHeader !== previousTableRowHeader) {
                previousTableRowHeader?.table = null

                field = tableRowHeader

                tableRowHeader?.table = this
            }
        }

    // Adaptation of JTable.configureEnclosingScrollPane()
    override fun configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane()

        withEnclosingScrollPane { scrollPane ->
            scrollPane.setRowHeaderView(tableRowHeader)

            configureEnclosingScrollPaneUI()
        }
    }

    private fun configureEnclosingScrollPaneUI() {
        withEnclosingScrollPane { scrollPane ->
            var corner = scrollPane.getCorner(JScrollPane.UPPER_LEADING_CORNER)
            if (corner == null || corner is UIResource) {
                corner = STableScrollPaneCorner()
                scrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, corner)
            }
        }
    }

    override fun unconfigureEnclosingScrollPane() {
        super.unconfigureEnclosingScrollPane()

        withEnclosingScrollPane { scrollPane ->
            scrollPane.setRowHeaderView(null)

            val corner = scrollPane.getCorner(JScrollPane.UPPER_LEADING_CORNER)
            if (corner is UIResource) {
                scrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, null)
            }
        }
    }

    // JTable has this code at least three times
    private inline fun withEnclosingScrollPane(block: (JScrollPane) -> Unit) {
        val parent = SwingUtilities.getUnwrappedParent(this)
        if (parent !is JViewport) return

        val scrollPane = parent.parent
        if (scrollPane !is JScrollPane) return

        val viewport = scrollPane.viewport
        if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) return

        block(scrollPane)
    }

    override fun setUI(ui: TableUI?) {
        super.setUI(STableUI(this))
    }

    override fun updateUI() {
        super.updateUI()

        setTableHeader(STableHeader(columnModel))
        // normally done by UI
        tableRowHeader = STableRowHeader()

        tableRowHeader?.let { tableRowHeader ->
            if (tableRowHeader.parent == null) {
                tableRowHeader.updateUI()
            }
        }

        configureEnclosingScrollPaneUI()
    }

    init {
        installActions()
    }

    private fun installActions() {
        registerKeyboardAction(KeyStroke("ENTER"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, editAction("selectNextRowCell"))
        registerKeyboardAction(KeyStroke("shift ENTER"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, editAction("selectPreviousRowCell"))
    }

    private fun editAction(successorAction: String): ActionListener {
        return ActionListener { event ->
            if (isEditing) {
                cellEditor.stopCellEditing()

                actionMap.get(successorAction).actionPerformed(event)
            } else {
                val column = columnModel.selectionModel.leadSelectionIndex
                val row = selectionModel.leadSelectionIndex

                if (column != -1 && row != -1) {
                    editCellAt(row, column, event)
                }
            }
        }
    }
}
