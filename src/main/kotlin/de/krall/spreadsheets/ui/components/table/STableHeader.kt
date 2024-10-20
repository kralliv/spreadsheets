package de.krall.spreadsheets.ui.components.table

import de.krall.spreadsheets.ui.event.isLeftButton
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.plaf.TableHeaderUI
import javax.swing.table.JTableHeader
import javax.swing.table.TableColumnModel

class STableHeader(columnModel: TableColumnModel? = null) : JTableHeader(columnModel) {

    init {
        val mouseHandler = object : MouseAdapter() {
            private var startIndex = -1

            override fun mousePressed(e: MouseEvent) {
                if (!e.isLeftButton) return

                startIndex = columnAtPoint(e.point)

                performSelection(e)
            }

            override fun mouseDragged(e: MouseEvent) {
                if (!e.isLeftButton) return

                performSelection(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (!e.isLeftButton) return

                startIndex = -1
            }

            private fun performSelection(e: MouseEvent) {
                table?.let { table ->
                    val columnSelectionModel = table.columnModel.selectionModel

                    val endIndex = columnAtPoint(e.point)
                    columnSelectionModel.setSelectionInterval(startIndex, endIndex)

                    val rowSelectionModel = table.selectionModel

                    rowSelectionModel.addSelectionInterval(table.rowCount, 0)
                }
            }
        }

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)    }

    override fun setUI(ui: TableHeaderUI?) {
        super.setUI(STableHeaderUI(this))
    }
}
