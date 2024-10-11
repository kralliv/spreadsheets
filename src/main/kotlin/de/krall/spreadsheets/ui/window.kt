package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.SpreadsheetImpl
import de.krall.spreadsheets.ui.components.SScrollPane
import java.awt.Dimension
import javax.swing.JFrame

class DocumentWindow : JFrame() {

    private val table: SpreadsheetTable

    init {
        title = "New spreadsheet"
        size = Dimension(1200, 720)
        setLocationRelativeTo(null)

        val spreadsheet = SpreadsheetImpl()

        table = SpreadsheetTable(spreadsheet)

        contentPane.add(SScrollPane(table))
    }


}
