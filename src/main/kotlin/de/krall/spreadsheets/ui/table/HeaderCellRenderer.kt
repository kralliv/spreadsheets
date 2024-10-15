package de.krall.spreadsheets.ui.table

import de.krall.spreadsheets.ui.components.SRendererLabel
import fernice.reflare.classes
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class HeaderCellRenderer : SRendererLabel(), TableCellRenderer {

    init {
        horizontalAlignment = CENTER
        classes.add("s-table-header")
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {

        text = value?.toString() ?: ""

        return this
    }
}