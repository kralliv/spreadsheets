package de.krall.spreadsheets.ui

import fernice.reflare.classes
import java.awt.Component
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor

class ValueCellEditor : AbstractCellEditor(), TableCellEditor {

    private val field = ValueField()

    init {
        field.classes.add("value-field-cell")
    }

    override fun stopCellEditing(): Boolean {
        return super.stopCellEditing()
    }

    override fun getCellEditorValue(): Any? {
        return field.text
    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int,
    ): Component {
        field.text = value as String? ?: ""
        return field
    }
}
