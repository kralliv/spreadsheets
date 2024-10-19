package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.SpreadsheetImpl
import de.krall.spreadsheets.sheet.io.readFrom
import de.krall.spreadsheets.ui.dialog.SMessageDialog
import de.krall.spreadsheets.ui.file.FileChoosers
import de.krall.spreadsheets.value.parser.ValueParser
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Component
import java.awt.Desktop
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Path
import kotlin.io.path.inputStream

class SpreadsheetManager {

    private val parser = ValueParser()

    private val overviewWindow = OverviewWindow(this)
    private val spreadsheetWindows = mutableListOf<SpreadsheetWindow>()

    fun createSpreadsheet() {
        val spreadsheet = SpreadsheetImpl(parser)

        val window = SpreadsheetWindow(this, spreadsheet, file = null)

        show(window)
    }

    fun openSpreadsheet(file: Path) {
        val spreadsheet = SpreadsheetImpl(parser)

        try {
            file.inputStream().use { inputStream ->
                spreadsheet.readFrom(inputStream)
            }
        } catch (exception: Exception) {
            LOG.error(exception) { "failed to read spreadsheet from file '$file'" }
            SMessageDialog("Error", "Unable to open spreadsheet file.").show(null)
            return
        }

        val window = SpreadsheetWindow(this, spreadsheet, file)

        show(window)
    }

    private fun show(window: SpreadsheetWindow) {
        window.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                spreadsheetWindows.remove(window)
                updateOverviewState()
            }
        })

        spreadsheetWindows.add(window)
        updateOverviewState()

        window.isVisible = true
    }

    private fun updateOverviewState() {
        overviewWindow.isVisible = spreadsheetWindows.isEmpty()
    }

    fun initialize() {
        updateOverviewState()
    }

    init {
        Desktop.getDesktop().setQuitHandler { _, response ->
            if (spreadsheetWindows.toList().all { it.close() }) {
                response.performQuit()
            } else {
                response.cancelQuit()
            }
        }
    }

    companion object {
        private val LOG = KotlinLogging.logger { }
    }
}

fun SpreadsheetManager.chooseSpreadsheet(invoker: Component?) {
    val file = FileChoosers.showFileChooser(invoker)
    if (file != null) {
        openSpreadsheet(file.toPath())
    }
}
