package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.SpreadsheetImpl
import de.krall.spreadsheets.sheet.io.readFrom
import de.krall.spreadsheets.sheet.io.writeTo
import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.components.SScrollPane
import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.file.FileChooser
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.classes
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame

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

        container.registerKeyboardAction(KeyStroke("ctrl S", macos = "command S"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            val file = FileChooser(this)

            if (file != null) {
                file.outputStream().use { outputStream ->
                    spreadsheet.writeTo(outputStream)
                }
            }
        }

        container.registerKeyboardAction(KeyStroke("ctrl O", macos = "command O"), Conditions.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            val file = FileChooser(this)

            if (file != null && file.exists()) {
                file.inputStream().use { inputStream ->
                    spreadsheet.readFrom(inputStream)
                }
            }
        }
    }
}
