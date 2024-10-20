package de.krall.spreadsheets.ui.components.table

import de.krall.spreadsheets.ui.util.drawAALine
import org.fernice.flare.selector.NonTSPseudoClass
import org.fernice.reflare.element.element
import org.fernice.reflare.ui.FlareTableUI
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.TableColumn
import kotlin.math.min

class STableUI(table: JTable) : FlareTableUI(table) {

    override fun paint(g: Graphics, c: JComponent) {
        val clip = g.clipBounds

        val bounds = table.bounds
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.y = 0
        bounds.x = bounds.y

        if (table.rowCount <= 0 || table.columnCount <= 0 ||
            // this check prevents us from painting the entire table
            // when the clip doesn't intersect our bounds at all
            !bounds.intersects(clip)
        ) {

            paintDropLines(g)
            return
        }

        val ltr = table.componentOrientation.isLeftToRight

        val upperLeft = clip.location
        val lowerRight = Point(
            clip.x + clip.width - 1,
            clip.y + clip.height - 1
        )

        var rMin = table.rowAtPoint(upperLeft)
        var rMax = table.rowAtPoint(lowerRight)
        // This should never happen (as long as our bounds intersect the clip,
        // which is why we bail above if that is the case).
        if (rMin == -1) {
            rMin = 0
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // (We could also get -1 if our bounds don't intersect the clip,
        // which is why we bail above if that is the case).
        // Replace this with the index of the last row.
        if (rMax == -1) {
            rMax = table.rowCount - 1
        }

        var cMin = table.columnAtPoint(if (ltr) upperLeft else lowerRight)
        var cMax = table.columnAtPoint(if (ltr) lowerRight else upperLeft)
        // This should never happen.
        if (cMin == -1) {
            cMin = 0
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
            cMax = table.columnCount - 1
        }

        // Paint the grid.
        paintGrid(g, rMin, rMax, cMin, cMax)

        // Paint the cells.
        paintCells(g, rMin, rMax, cMin, cMax)

        paintDropLines(g)
    }

    private fun paintDropLines(g: Graphics) {
        val loc = table.dropLocation ?: return

        val color = UIManager.getColor("Table.dropLineColor")
        val shortColor = UIManager.getColor("Table.dropLineShortColor")
        if (color == null && shortColor == null) {
            return
        }

        var rect = getHDropLineRect(loc)
        if (rect != null) {
            val x = rect.x
            val w = rect.width
            if (color != null) {
                extendRect(rect, true)
                g.color = color
                g.fillRect(rect.x, rect.y, rect.width, rect.height)
            }
            if (!loc.isInsertColumn && shortColor != null) {
                g.color = shortColor
                g.fillRect(x, rect.y, w, rect.height)
            }
        }

        rect = getVDropLineRect(loc)
        if (rect != null) {
            val y = rect.y
            val h = rect.height
            if (color != null) {
                extendRect(rect, false)
                g.color = color
                g.fillRect(rect.x, rect.y, rect.width, rect.height)
            }
            if (!loc.isInsertRow && shortColor != null) {
                g.color = shortColor
                g.fillRect(rect.x, y, rect.width, h)
            }
        }
    }


    private fun getHDropLineRect(loc: JTable.DropLocation): Rectangle? {
        if (!loc.isInsertRow) {
            return null
        }

        var row = loc.row
        var col = loc.column
        if (col >= table.columnCount) {
            col--
        }

        val rect = table.getCellRect(row, col, true)

        if (row >= table.rowCount) {
            row--
            val prevRect = table.getCellRect(row, col, true)
            rect.y = prevRect.y + prevRect.height
        }

        if (rect.y == 0) {
            rect.y = -1
        } else {
            rect.y -= 2
        }

        rect.height = 3

        return rect
    }

    private fun getVDropLineRect(loc: JTable.DropLocation): Rectangle? {
        if (!loc.isInsertColumn) {
            return null
        }

        val ltr = table.componentOrientation.isLeftToRight
        var col = loc.column
        var rect = table.getCellRect(loc.row, col, true)

        if (col >= table.columnCount) {
            col--
            rect = table.getCellRect(loc.row, col, true)
            if (ltr) {
                rect.x = rect.x + rect.width
            }
        } else if (!ltr) {
            rect.x = rect.x + rect.width
        }

        if (rect.x == 0) {
            rect.x = -1
        } else {
            rect.x -= 2
        }

        rect.width = 3

        return rect
    }

    private fun extendRect(rect: Rectangle?, horizontal: Boolean): Rectangle? {
        if (rect == null) {
            return rect
        }

        if (horizontal) {
            rect.x = 0
            rect.width = table.width
        } else {
            rect.y = 0

            if (table.rowCount != 0) {
                val lastRect = table.getCellRect(table.rowCount - 1, 0, true)
                rect.height = lastRect.y + lastRect.height
            } else {
                rect.height = table.height
            }
        }

        return rect
    }

    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private fun paintGrid(g: Graphics, rMin: Int, rMax: Int, cMin: Int, cMax: Int) {
        g.color = table.gridColor

        val minCell = table.getCellRect(rMin, cMin, true)
        val maxCell = table.getCellRect(rMax, cMax, true)
        val damagedArea = minCell.union(maxCell)

        if (table.showHorizontalLines) {
            val tableWidth = damagedArea.x + damagedArea.width
            var y = damagedArea.y
            for (row in rMin..rMax) {
                y += table.getRowHeight(row)
                g.drawAALine(damagedArea.x, y - 1, tableWidth - 1, y - 1)
            }
        }
        if (table.showVerticalLines) {
            val cm = table.columnModel
            val tableHeight = damagedArea.y + damagedArea.height
            var x: Int
            if (table.componentOrientation.isLeftToRight) {
                x = damagedArea.x
                for (column in cMin..cMax) {
                    val w = cm.getColumn(column).width
                    x += w
                    g.drawAALine(x - 1, 0, x - 1, tableHeight - 1)
                }
            } else {
                x = damagedArea.x
                for (column in cMax downTo cMin) {
                    val w = cm.getColumn(column).width
                    x += w
                    g.drawAALine(x - 1, 0, x - 1, tableHeight - 1)
                }
            }
        }
    }

    private fun viewIndexForColumn(aColumn: TableColumn): Int {
        val cm = table.columnModel
        for (column in 0 until cm.columnCount) {
            if (cm.getColumn(column) === aColumn) {
                return column
            }
        }
        return -1
    }

    private fun paintCells(g: Graphics, rMin: Int, rMax: Int, cMin: Int, cMax: Int) {
        val header = table.tableHeader
        val draggedColumn = header?.draggedColumn

        val cm = table.columnModel
        val columnMargin = cm.columnMargin

        var cellRect: Rectangle
        var aColumn: TableColumn
        var columnWidth: Int
        if (table.componentOrientation.isLeftToRight) {
            for (row in rMin..rMax) {
                cellRect = table.getCellRect(row, cMin, false)
                for (column in cMin..cMax) {
                    aColumn = cm.getColumn(column)
                    columnWidth = aColumn.width
                    cellRect.width = columnWidth - columnMargin
                    if (aColumn !== draggedColumn) {
                        paintCell(g, cellRect, row, column)
                    }
                    cellRect.x += columnWidth
                }
            }
        } else {
            for (row in rMin..rMax) {
                cellRect = table.getCellRect(row, cMin, false)
                aColumn = cm.getColumn(cMin)
                if (aColumn !== draggedColumn) {
                    columnWidth = aColumn.width
                    cellRect.width = columnWidth - columnMargin
                    paintCell(g, cellRect, row, cMin)
                }
                for (column in cMin + 1..cMax) {
                    aColumn = cm.getColumn(column)
                    columnWidth = aColumn.width
                    cellRect.width = columnWidth - columnMargin
                    cellRect.x -= columnWidth
                    if (aColumn !== draggedColumn) {
                        paintCell(g, cellRect, row, column)
                    }
                }
            }
        }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
            paintDraggedArea(g, rMin, rMax, draggedColumn, header.draggedDistance)
        }

        // Remove any renderers that may be left in the rendererPane.
        rendererPane.removeAll()
    }

    private fun paintDraggedArea(g: Graphics, rMin: Int, rMax: Int, draggedColumn: TableColumn, distance: Int) {
        val draggedColumnIndex = viewIndexForColumn(draggedColumn)

        val minCell = table.getCellRect(rMin, draggedColumnIndex, true)
        val maxCell = table.getCellRect(rMax, draggedColumnIndex, true)

        val vacatedColumnRect = minCell.union(maxCell)

        // Paint a gray well in place of the moving column.
        g.color = table.parent.background
        g.fillRect(
            vacatedColumnRect.x, vacatedColumnRect.y,
            vacatedColumnRect.width, vacatedColumnRect.height
        )

        // Move to the where the cell has been dragged.
        vacatedColumnRect.x += distance

        // Fill the background.
        g.color = table.background
        g.fillRect(
            vacatedColumnRect.x, vacatedColumnRect.y,
            vacatedColumnRect.width, vacatedColumnRect.height
        )

        // Paint the vertical grid lines if necessary.
        if (table.showVerticalLines) {
            g.color = table.gridColor
            val x1 = vacatedColumnRect.x
            val y1 = vacatedColumnRect.y
            val x2 = x1 + vacatedColumnRect.width - 1
            val y2 = y1 + vacatedColumnRect.height - 1
            // Left
            g.drawAALine(x1 - 1, y1, x1 - 1, y2)
            // Right
            g.drawAALine(x2, y1, x2, y2)
        }

        for (row in rMin..rMax) {
            // Render the cell value
            val r = table.getCellRect(row, draggedColumnIndex, false)
            r.x += distance
            paintCell(g, r, row, draggedColumnIndex)

            // Paint the (lower) horizontal grid line if necessary.
            if (table.showHorizontalLines) {
                g.color = table.gridColor
                val rcr = table.getCellRect(row, draggedColumnIndex, true)
                rcr.x += distance
                val x1 = rcr.x
                val y1 = rcr.y
                val x2 = x1 + rcr.width - 1
                val y2 = y1 + rcr.height - 1
                g.drawAALine(x1, y2, x2, y2)
            }
        }
    }

    private fun paintCell(g: Graphics, cellRect: Rectangle, row: Int, column: Int) {
        if (table.isEditing && table.editingRow == row &&
            table.editingColumn == column
        ) {
            val component = table.editorComponent

            // Allow the editor to grow a bit
            val preferredSize = component.preferredSize

            val preferredWidth = min(preferredSize.width, MAX_EDITOR_EXTEND)
            var width = cellRect.width
            for (nextColumn in (column + 1)..<table.columnCount) {
                if (width >= preferredWidth) break
                val nextCellRect = table.getCellRect(row, nextColumn, true)
                width += nextCellRect.width
            }

            component.setBounds(cellRect.x, cellRect.y, width, cellRect.height)
            component.validate()
        } else {
            val renderer = table.getCellRenderer(row, column)
            val component = table.prepareRenderer(renderer, row, column)

            val columnSelectionModel = table.columnModel.selectionModel
            val rowSelectionModel = table.selectionModel

            var isSelected = false
            var hasFocus = false

            // Only indicate the selection and focused cell if not printing
            if (!table.isPaintingForPrint) {
                isSelected = table.isCellSelected(row, column)

                val rowIsLead = rowSelectionModel.leadSelectionIndex == row
                val colIsLead = columnSelectionModel.leadSelectionIndex == column

                hasFocus = rowIsLead && colIsLead && table.isFocusOwner
            }

            val element = component.element

            element.hint(NonTSPseudoClass.Active, isSelected)
            element.hint(NonTSPseudoClass.Focus, hasFocus)

            rendererPane.paintComponent(
                g, component, table, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true
            )
        }
    }

    companion object {
        const val MAX_EDITOR_EXTEND = 400
    }
}
