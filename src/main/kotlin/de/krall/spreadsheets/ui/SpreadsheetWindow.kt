package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.Cell
import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.sheet.SpreadsheetListener
import de.krall.spreadsheets.sheet.io.writeTo
import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.components.SScrollPane
import de.krall.spreadsheets.ui.dialog.DialogTemplate
import de.krall.spreadsheets.ui.dialog.SDialogContent
import de.krall.spreadsheets.ui.dialog.SMessageDialog
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.ui.env.OS
import fernice.reflare.classes
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Path
import javax.swing.JFrame
import kotlin.io.path.outputStream

class SpreadsheetWindow(
    spreadsheetManager: SpreadsheetManager,
    private val spreadsheet: Spreadsheet,
    parser: ValueParser,
    file: Path?,
) : JFrame() {

    val table: SpreadsheetTable

    private var file: Path? = file
        set(file) {
            field = file

            updateTitle()
        }

    private var isModified: Boolean = false
        set(modified) {
            field = modified

            updateTitle()
        }

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        jMenuBar = SpreadsheetMenuBar(spreadsheetManager, this)

        size = Dimension(1200, 720)
        setLocationRelativeTo(null)

        table = SpreadsheetTable(spreadsheet, parser)

        val scrollPane = SScrollPane(table)
        scrollPane.viewport.border = null

        val container = SContainer()
        container.layout = BorderLayout()
        container.classes.add("s-root")
        container.add(scrollPane)

        contentPane = container

        updateTitle()

        spreadsheet.addListener(object : SpreadsheetListener {
            override fun cellChanged(cell: Cell, previousCell: Cell) {
                isModified = true
            }

            override fun cellUpdated(cell: Cell) {
                // updates are volatile aka. visual
            }
        })

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                close()
            }
        })
    }

    private fun updateTitle() {
        val file = file
        val isModified = isModified

        var title = file?.fileName?.toString() ?: "New spreadsheet"
        if (isModified && !OS.isMac) {
            title = "$title*"
        }
        this.title = title

        rootPane.putClientProperty("Window.documentModified", isModified)
    }

    fun save(): Boolean {
        var file = this.file
        if (file == null) {
            file = SaveDialog().showAndGet(this)
                ?: return false

            this.file = file
        }

        return saveTo(file)
    }

    fun saveAs(): Boolean {
        val file = SaveDialog().showAndGet(this)
            ?: return false

        this.file = file

        return saveTo(file)
    }

    private fun saveTo(file: Path): Boolean {
        try {
            file.outputStream().use { outputStream ->
                spreadsheet.writeTo(outputStream)
            }
        } catch (exception: Exception) {
            LOG.error(exception) { "failed to read spreadsheet from file '$file'" }
            SMessageDialog("Error", "Unable to save spreadsheet to file.").show(this)
            return false
        }
        isModified = false
        return true
    }

    fun close(): Boolean {
        if (isModified) {
            val outcome = SMessageDialog(
                "Unsaved changes",
                "There are unsaved changes. Would you like to save them?",
                DialogTemplate.SAVE_DISCARD_CANCEL,
            ).showAndRetrieve(this@SpreadsheetWindow)

            if (outcome == SDialogContent.NEUTRAL) return false
            if (outcome == SDialogContent.POSITIVE) {
                if (!save()) return false
            }
        }

        spreadsheet.close()
        dispose()
        return true
    }

    companion object {
        private val LOG = KotlinLogging.logger { }
    }
}
