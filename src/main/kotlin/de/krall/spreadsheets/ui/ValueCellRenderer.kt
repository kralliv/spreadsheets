package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.Spreadsheet
import de.krall.spreadsheets.ui.components.SRendererLabel
import de.krall.spreadsheets.ui.util.useGraphics2D
import de.krall.spreadsheets.value.ComputationError
import de.krall.spreadsheets.value.EvaluatedValue
import fernice.reflare.classes
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Shape
import java.awt.geom.Path2D
import java.text.DecimalFormat
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.TableCellRenderer
import kotlin.math.log2

class ValueCellRenderer(val spreadsheet: Spreadsheet) : TableCellRenderer {

    private val numberFormat = DecimalFormat().apply {
        minimumIntegerDigits = 1
        minimumFractionDigits = 0
        maximumFractionDigits = Int.MAX_VALUE
    }

    private val valueLabel = ValueLabel()

    init {
        valueLabel.classes.add("s-table-cell-renderer")
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        assert(column == 0) { "renderer should not be used for the first non-value column" }

        val cell = spreadsheet[column, row]

        val evaluatedValue = cell.evaluatedValue

        valueLabel.text = when (evaluatedValue) {
            null -> ""
            is EvaluatedValue.Text -> evaluatedValue.text
            is EvaluatedValue.Number -> numberFormat.format(evaluatedValue.number)
            is EvaluatedValue.Unevaluated -> "evaluating..."
            is EvaluatedValue.Error -> when (evaluatedValue.error) {
                is ComputationError.BadFormula -> "#INVALID"
                is ComputationError.CircularDependency -> "#REF"
                is ComputationError.DivisionByZero -> "#DIV/0"
            }
        }

        valueLabel.horizontalAlignment = when (evaluatedValue) {
            is EvaluatedValue.Number -> SwingConstants.RIGHT
            else -> SwingConstants.LEADING
        }

        valueLabel.isErroneous = when (evaluatedValue) {
            is EvaluatedValue.Error -> true
            else -> false
        }

        handleSelectionBorder(table, isSelected, hasFocus, row, column, valueLabel)

        return valueLabel
    }

    private fun handleSelectionBorder(table: JTable, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int, component: Component) {
        val columnSelectionModel = table.columnModel.selectionModel
        val rowSelectionModel = table.selectionModel

        if (isSelected && !hasFocus && columnSelectionModel.isSelectedIndex(column)
            && (column == 0 || !columnSelectionModel.isSelectedIndex(column - 1))
        ) {
            component.classes.add("t-table-cell-renderer-left")
        } else {
            component.classes.remove("t-table-cell-renderer-left")
        }

        if (isSelected && !hasFocus && columnSelectionModel.isSelectedIndex(column)
            && (column == table.columnModel.columnCount - 1 || !columnSelectionModel.isSelectedIndex(column + 1))
        ) {
            component.classes.add("t-table-cell-renderer-right")
        } else {
            component.classes.remove("t-table-cell-renderer-right")
        }

        if (isSelected && !hasFocus && rowSelectionModel.isSelectedIndex(row)
            && (row == 0 || !rowSelectionModel.isSelectedIndex(row - 1))
        ) {
            component.classes.add("t-table-cell-renderer-top")
        } else {
            component.classes.remove("t-table-cell-renderer-top")
        }

        if (isSelected && !hasFocus && rowSelectionModel.isSelectedIndex(row)
            && (row == table.model.rowCount - 1 || !rowSelectionModel.isSelectedIndex(row + 1))
        ) {
            component.classes.add("t-table-cell-renderer-bottom")
        } else {
            component.classes.remove("t-table-cell-renderer-bottom")
        }
    }
}

private class ValueLabel : SRendererLabel() {

    var isErroneous: Boolean = false

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (isErroneous) {
            g.useGraphics2D { g2 ->
                g2.color = Color.RED
                g2.fill(createCorner())
            }
        }
    }

    private fun createCorner(): Shape {
        val path = Path2D.Double()

        path.moveTo(width.toDouble() - CORNER_SIZE.toDouble(), 0.0)
        path.lineTo(width.toDouble(), 0.0)
        path.lineTo(width.toDouble(), CORNER_SIZE.toDouble())
        path.closePath()

        return path
    }

    companion object {
        private const val CORNER_SIZE = 7
    }
}
