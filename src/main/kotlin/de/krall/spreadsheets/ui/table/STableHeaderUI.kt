package de.krall.spreadsheets.ui.table

import de.krall.spreadsheets.ui.util.drawAALine
import org.fernice.flare.selector.NonTSPseudoClass
import org.fernice.reflare.element.element
import org.fernice.reflare.ui.FlareTableHeaderUI
import org.fernice.reflare.ui.FlareUI
import java.awt.BasicStroke
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.JTableHeader
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

class STableHeaderUI(tableHeader: JTableHeader) : FlareTableHeaderUI(tableHeader) {

    override fun paint(g: Graphics, c: JComponent) {
        if (header.columnModel.columnCount <= 0) {
            return
        }
        val ltr = header.componentOrientation.isLeftToRight

        val clip = g.clipBounds
        val left = clip.location
        val right = Point(clip.x + clip.width - 1, clip.y)
        val cm = header.columnModel
        var cMin = header.columnAtPoint(if (ltr) left else right)
        var cMax = header.columnAtPoint(if (ltr) right else left)
        // This should never happen.
        if (cMin == -1) {
            cMin = 0
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
            cMax = cm.columnCount - 1
        }

        val draggedColumn = header.draggedColumn
        var columnWidth: Int
        val cellRect = header.getHeaderRect(if (ltr) cMin else cMax)
        var aColumn: TableColumn
        val table = header.table
        if (ltr) {
            for (column in cMin..cMax) {
                aColumn = cm.getColumn(column)
                columnWidth = aColumn.width
                cellRect.width = columnWidth
                if (aColumn !== draggedColumn) {
                    paintCell(g, cellRect, column)
                }
                cellRect.x += columnWidth
            }
        } else {
            for (column in cMax downTo cMin) {
                aColumn = cm.getColumn(column)
                columnWidth = aColumn.width
                cellRect.width = columnWidth
                if (aColumn !== draggedColumn) {
                    paintCell(g, cellRect, column)
                }
                cellRect.x += columnWidth
            }
        }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
            val draggedColumnIndex = viewIndexForColumn(draggedColumn)
            val draggedCellRect = header.getHeaderRect(draggedColumnIndex)

            // Draw a gray well in place of the moving column.
            g.color = header.parent.background
            g.fillRect(
                draggedCellRect.x, draggedCellRect.y,
                draggedCellRect.width, draggedCellRect.height
            )

            draggedCellRect.x += header.draggedDistance

            // Fill the background.
            g.color = header.background
            g.fillRect(
                draggedCellRect.x, draggedCellRect.y,
                draggedCellRect.width, draggedCellRect.height
            )

            paintCell(g, draggedCellRect, draggedColumnIndex)
        }
        // Remove all components in the rendererPane.
        rendererPane.removeAll()
    }

    private fun getHeaderRenderer(columnIndex: Int): Component {
        val aColumn = header.columnModel.getColumn(columnIndex)
        var renderer: TableCellRenderer? = aColumn.headerRenderer
        if (renderer == null) {
            renderer = header.defaultRenderer!!
        }

        val selectionModel = header.columnModel.selectionModel

        var isSelected = false
        var hasFocus = false

        if (!header.isPaintingForPrint) {
            isSelected = selectionModel.isSelectedIndex(columnIndex)
            hasFocus = columnIndex == selectionModel.leadSelectionIndex && header.hasFocus()
        }

        val component = renderer.getTableCellRendererComponent(
            header.table,
            aColumn.headerValue,
            isSelected, hasFocus,
            -1, columnIndex
        )

        val element = component.element

        element.hint(NonTSPseudoClass.Hover, columnIndex == rolloverColumn)
        element.hint(NonTSPseudoClass.Active, isSelected)
        element.hint(NonTSPseudoClass.Focus, hasFocus)

        return component
    }

    private fun paintCell(g: Graphics, cellRect: Rectangle, columnIndex: Int) {
        val component = getHeaderRenderer(columnIndex)

        val table = header.table

        val verticalGap = if (table?.showVerticalLines == true) 1 else 0
        val horizontalGap = 1 // if (table?.showHorizontalLines == true) 1 else 0

        rendererPane.paintComponent(
            g, component, header, cellRect.x, cellRect.y,
            cellRect.width - verticalGap, cellRect.height - horizontalGap, true
        )

        paintCellGrid(g, table, columnIndex, cellRect)
    }

    private fun paintCellGrid(g: Graphics, table: JTable?, column: Int, cellRect: Rectangle) {
        if (table != null) {
            val g2 = g as Graphics2D

            g2.stroke = BasicStroke(1f)

            val draggedColumn = header.draggedColumn
            if (draggedColumn != null && table.showVerticalLines && column == viewIndexForColumn(draggedColumn)) {
                g.color = table.gridColor
                g.drawAALine(cellRect.x - 1, cellRect.y, cellRect.x - 1, cellRect.y + cellRect.height - 1)
            }

            if (table.showVerticalLines) {
                g.color = table.gridColor
                g.drawAALine(cellRect.x + cellRect.width - 1, cellRect.y, cellRect.x + cellRect.width - 1, cellRect.y + cellRect.height - 1)
            }

            // if (table.showHorizontalLines) {
            g.color = table.gridColor
            g.drawAALine(cellRect.x, cellRect.y + cellRect.height - 1, cellRect.x + cellRect.width - 1, cellRect.y + cellRect.height - 1)
            // }
        }
    }

    private fun viewIndexForColumn(aColumn: TableColumn): Int {
        val cm = header.columnModel
        for (column in 0 until cm.columnCount) {
            if (cm.getColumn(column) === aColumn) {
                return column
            }
        }
        return -1
    }
}

