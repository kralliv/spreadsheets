package de.krall.spreadsheets.ui

import de.krall.spreadsheets.value.Value
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.classes
import java.awt.Component
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor

class ValueCellEditor(val parser: ValueParser) : AbstractCellEditor(), TableCellEditor {

    private val field = ValueField(parser)

    init {
        field.classes.add("value-field-cell")
    }

    override fun stopCellEditing(): Boolean {
        return super.stopCellEditing()
    }

    override fun getCellEditorValue(): Any? {
        val text = field.text
        if (text.isEmpty()) return null
        return parser.parseValue(text)
    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int,
    ): Component {
        value as Value?

        field.text = when (value) {
            null -> ""
            is Value.Text -> value.text
            is Value.Number -> value.number.toString()
            is Value.Formula -> "=${value.formula}"
        }
        return field
    }
}
