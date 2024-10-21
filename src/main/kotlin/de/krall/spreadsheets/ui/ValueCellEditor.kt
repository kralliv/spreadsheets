package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.deregisterKeyboardAction
import fernice.reflare.classes
import java.awt.Component
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.KeyStroke
import javax.swing.table.TableCellEditor

class ValueCellEditor(val parser: ValueParser) : AbstractCellEditor(), TableCellEditor {

    private val field = EditorValueField(parser)

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

    private class EditorValueField(parser: ValueParser) : ValueField(parser) {

        init {
            columns = 0
            classes.add("s-table-cell-editor")
            deregisterKeyboardAction(KeyStroke("ENTER"))
            deregisterKeyboardAction(KeyStroke("ESCAPE"))
        }

        override fun processKeyBinding(ks: KeyStroke, e: KeyEvent, condition: Int, pressed: Boolean): Boolean {
            // The table starts editing on typing. This clears the field
            // before the first input, causing the feeling of overwriting.
            if (condition == WHEN_FOCUSED && !isFocusOwner && e.id == KeyEvent.KEY_PRESSED) {
                value = null
            }

            return super.processKeyBinding(ks, e, condition, pressed)
        }
    }
}
