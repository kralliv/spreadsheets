package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.sheet.value.ComputationError
import de.krall.spreadsheets.sheet.value.EvaluatedValue
import de.krall.spreadsheets.ui.components.SRendererLabel
import de.krall.spreadsheets.ui.render.StringRenderable
import fernice.reflare.classes
import org.fernice.reflare.render.use
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Point
import java.awt.Shape
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import java.text.DecimalFormat
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueCellRenderer(val spreadsheet: Spreadsheet) : SRendererLabel(), TableCellRenderer, StringRenderable {

    private val numberFormat = DecimalFormat().apply {
        minimumIntegerDigits = 1
        minimumFractionDigits = 0
        maximumFractionDigits = Int.MAX_VALUE
    }

    init {
        classes.add("s-table-cell-renderer")
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val cell = spreadsheet[table.convertRowIndexToModel(row), table.convertColumnIndexToModel(column)]

        val evaluatedValue = cell.evaluatedValue

        text = when (evaluatedValue) {
            null -> ""
            is EvaluatedValue.Text -> evaluatedValue.text
            is EvaluatedValue.Number -> numberFormat.format(evaluatedValue.number)
            is EvaluatedValue.Unevaluated -> "evaluating..."
            is EvaluatedValue.Error -> when (evaluatedValue.error) {
                is ComputationError.BadFormula -> "#INVALID"
                is ComputationError.CircularDependency -> "#REF"
                is ComputationError.DivisionByZero -> "#DIV/0"
                is ComputationError.Error -> "#ERROR"
            }
        }

        horizontalAlignment = when (evaluatedValue) {
            is EvaluatedValue.Number -> RIGHT
            else -> LEADING
        }

        isErroneous = when (evaluatedValue) {
            is EvaluatedValue.Error -> true
            else -> false
        }

        toolTipText = when (evaluatedValue) {
            null -> null
            is EvaluatedValue.Text -> null
            is EvaluatedValue.Number -> null
            is EvaluatedValue.Unevaluated -> "This cell is currently being evaluated."
            is EvaluatedValue.Error -> when (evaluatedValue.error) {
                is ComputationError.BadFormula -> "The entered formula is invalid."
                is ComputationError.CircularDependency -> "There are circular dependencies. Formulas must not reference each other."
                is ComputationError.DivisionByZero -> "The evaluation encountered a division by zero."
                is ComputationError.Error -> "The evaluation encountered an error."
            }
        }

        handleSelectionBorder(table, isSelected, hasFocus, row, column, this)

        return this
    }

    var isErroneous: Boolean = false

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (isErroneous) {
            g.use { g2 ->
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

    override fun renderToString(): String = text

    override fun getToolTipLocation(event: MouseEvent): Point? {
        if (toolTipText == null) return null
        return Point(width, 0)
    }

    companion object {
        private const val CORNER_SIZE = 7
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
