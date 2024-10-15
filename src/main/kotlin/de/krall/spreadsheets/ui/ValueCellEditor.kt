package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.deregisterKeyboardAction
import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.classes
import java.awt.Component
import java.awt.event.HierarchyEvent
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor

class ValueCellEditor(val parser: ValueParser) : AbstractCellEditor(), TableCellEditor {

    private val field = ValueField(parser)

    init {
        field.columns = 0
        field.classes.add("s-table-cell-editor")
        field.deregisterKeyboardAction(KeyStroke("ENTER"))
        field.addHierarchyListener { event ->
            if (event.id == HierarchyEvent.HIERARCHY_CHANGED
                && (event.changeFlags.toInt() and HierarchyEvent.SHOWING_CHANGED) != 0
                && field.isShowing
            ) {
                field.requestFocusInWindow()
            }
        }
    }

    override fun isCellEditable(event: EventObject?): Boolean {
        if (event is MouseEvent) {
            return event.clickCount >= 2
        }
        return true
    }

    override fun stopCellEditing(): Boolean {
        field.commitEdit()
        return super.stopCellEditing()
    }

    override fun getCellEditorValue(): Any? {
        return field.value
    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int,
    ): Component {
        field.value = value as Value?
        return field
    }
}
