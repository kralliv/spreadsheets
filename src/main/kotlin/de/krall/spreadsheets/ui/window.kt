package de.krall.spreadsheets.ui

import de.krall.spreadsheets.model.SpreadsheetImpl
import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.components.SScrollPane
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.classes
import fernice.reflare.style
import java.awt.BorderLayout
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

        val scrollPane = SScrollPane(table)
        scrollPane.viewport.border = null

        val container = SContainer()
        container.layout = BorderLayout()
        container.classes.add("s-root")
        container.add(scrollPane)

        contentPane = container
    }


}
