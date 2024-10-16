package de.krall.spreadsheets.ui.render

import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

fun TableCellRenderer.renderToString(
    table: JTable,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int,
): String {
    val component = getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

    return when (component) {
        is StringRenderable -> component.renderToString()
        is JLabel -> component.text
        else -> ""
    }
}
