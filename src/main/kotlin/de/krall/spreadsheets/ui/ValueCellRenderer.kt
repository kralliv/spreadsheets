package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.ui.components.SRendererLabel
import de.krall.spreadsheets.value.EvaluatedValue
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueCellRenderer(val spreadsheet: Spreadsheet) : TableCellRenderer {

    private val valueLabel = SRendererLabel()

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        assert(column == 0) { "renderer should not be used for the first non-value column" }

        val cell = spreadsheet[row, column - 1]

        valueLabel.text = when (val value = cell.evaluatedValue) {
            null -> ""
            is EvaluatedValue.Text -> value.text
            is EvaluatedValue.Number -> value.number.toString()
            is EvaluatedValue.Unevaluated -> "evaluating..."
            is EvaluatedValue.BadFormula -> "#INVALID"
            is EvaluatedValue.CircularDependencies -> "#REF"
        }

        return valueLabel
    }

    private fun JTable.repaint(row: Int, column: Int) {
        val bounds = getCellRect(row, column, false)
        repaint(bounds)
    }
}