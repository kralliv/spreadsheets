package de.krall.spreadsheets.ui.table

import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import org.jdesktop.swingx.JXTable
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.KeyStroke
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
        registerKeyboardAction(KeyStroke("BACK_SPACE"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            val model = model
            selectedRows.forEach { row ->
                selectedColumns.forEach { column ->
                    model.setValueAt(null, row, column)
                }
            }
        }
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

    override fun processKeyBinding(ks: KeyStroke, e: KeyEvent, condition: Int, pressed: Boolean): Boolean {
        if (processKeyBindingDirectly(ks, e, condition, pressed)) {
            return true
        }

        // "Why not start editing when pressing keys like shift?" said Swing
        if (e.id == KeyEvent.KEY_PRESSED
            && (ks.keyCode == 0
                    || ks.keyCode == KeyEvent.VK_ESCAPE
                    || ks.keyCode == KeyEvent.VK_ENTER
                    || ks.keyCode == KeyEvent.VK_CAPS_LOCK
                    || ks.keyCode == KeyEvent.VK_BACK_SPACE)
        ) {
            return false
        }

        return super.processKeyBinding(ks, e, condition, pressed)
    }

    private fun processKeyBindingDirectly(ks: KeyStroke, e: KeyEvent, condition: Int, pressed: Boolean): Boolean {
        val map = getInputMap(condition)
        val am = actionMap

        if (map != null && am != null && isEnabled) {
            val binding = map.get(ks)
            val action = binding?.let { am.get(it) }
            if (action != null) {
                return SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers())
            }
        }
        return false
    }
}
