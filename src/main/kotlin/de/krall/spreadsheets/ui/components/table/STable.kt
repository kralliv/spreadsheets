package de.krall.spreadsheets.ui.components.table

import de.krall.spreadsheets.ui.OS
import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import org.jdesktop.swingx.JXTable
import java.awt.Point
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.plaf.TableUI
import javax.swing.plaf.UIResource

private val IGNORABLE_KEY_CODES = setOf(KeyEvent.VK_UNDEFINED, KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_BACK_SPACE)

open class STable : JXTable() {

    init {
        surrendersFocusOnKeystroke = true
    }

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

    override fun removeEditor() {
        val editing = getCellEditor() != null
        val editingRow = editingRow
        val editingColumn = editingColumn

        super.removeEditor()

        if (editing) {
            val startCellRect = getCellRect(editingRow, editingColumn, false)

            var endCellRect = startCellRect
            for (nextColumn in (editingColumn + 1)..<columnCount) {
                if ((endCellRect.x + endCellRect.width) - startCellRect.x >= STableUI.MAX_EDITOR_EXTEND) break
                endCellRect = getCellRect(editingRow, nextColumn, true)
            }

            val cellRect = startCellRect.union(endCellRect)

            repaint(cellRect)
        }
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
        if (e.id == KeyEvent.KEY_PRESSED) {
            if (ks.keyCode in IGNORABLE_KEY_CODES) return false
            if (e.isControlDown || (OS.isMac && e.isMetaDown)) return false
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

    override fun getToolTipLocation(event: MouseEvent): Point? {
        val p = event.getPoint()

        // Locate the renderer under the event location
        val hitColumnIndex = columnAtPoint(p)
        val hitRowIndex = rowAtPoint(p)

        if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
            val renderer = getCellRenderer(hitRowIndex, hitColumnIndex)
            val component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex)

            // Now have to see if the component is a JComponent before
            // getting the tip
            if (component is JComponent) {
                // Convert the event to the renderer's coordinate system
                val cellRect = getCellRect(hitRowIndex, hitColumnIndex, true)
                p.translate(-cellRect.x, -cellRect.y)

                val modifiers = event.modifiersEx
                val newEvent = MouseEvent(
                    component, event.getID(),
                    event.getWhen(), modifiers,
                    p.x, p.y,
                    event.xOnScreen,
                    event.yOnScreen,
                    event.getClickCount(),
                    event.isPopupTrigger,
                    MouseEvent.NOBUTTON
                )

                val tooltip = component.getToolTipText(newEvent)
                if (tooltip != null) {
                    return Point(cellRect.x + cellRect.width, cellRect.y)
                }
            }
        }
        return null
    }
}
