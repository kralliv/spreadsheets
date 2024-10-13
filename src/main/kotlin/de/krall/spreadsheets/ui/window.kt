package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.SpreadsheetImpl
import de.krall.spreadsheets.ui.components.SScrollPane
import de.krall.spreadsheets.value.parser.ValueParser
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants

class DocumentWindow : JFrame() {

    private val table: SpreadsheetTable

    init {
        title = "New spreadsheet"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1200, 720)
        setLocationRelativeTo(null)

        val parser = ValueParser()

        val spreadsheet = SpreadsheetImpl(parser)

        table = SpreadsheetTable(spreadsheet, parser)

        contentPane.add(SScrollPane(table))
    }


}
