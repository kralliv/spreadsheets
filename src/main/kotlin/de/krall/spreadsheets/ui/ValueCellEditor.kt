package de.krall.spreadsheets.ui

import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.classes
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor

class ValueCellEditor(val parser: ValueParser) : AbstractCellEditor(), TableCellEditor {

    private val field = ValueField(parser)

    init {
        field.classes.add("s-table-cell-editor")
        field.addActionListener { fireEditingStopped() }
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
