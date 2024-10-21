package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.dialog.FileDialogs
import de.krall.spreadsheets.ui.env.Session
import de.krall.spreadsheets.ui.icon.Icons
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import javax.swing.text.DefaultFormatterFactory
import kotlin.io.path.Path

class SDirectoryField : SFormattedTextField() {

    init {
        formatterFactory = DefaultFormatterFactory(DirectoryFormatter())

        val chooserButton = SIconButton(Icons.Symbols.Directory)
        chooserButton.addActionListener { pickDirectory() }
        helper.trailingComponent = chooserButton

        installKeyboardActions()

        value = Session.lastDirectory
    }

    private fun pickDirectory() {
        val file = FileDialogs.selectDirectory(this, initial = value?.toFile())
        if (file != null) {
            value = file.toPath()
        }
    }

    private fun installKeyboardActions() {
        registerKeyboardAction(KeyStroke("ENTER")) {
            commitEdit()

            fireActionPerformed()
        }
    }

    private inner class DirectoryFormatter : AbstractFormatter() {

        override fun stringToValue(text: String?): Any? {
            if (text.isNullOrEmpty()) return null
            return try {
                Path(text)
            } catch (exception: Exception) {
                LOG.error(exception) { "failed to convert text to path: '$text'" }
                null
            }
        }

        override fun valueToString(value: Any?): String {
            if (value == null || value !is Path) return ""
            return value.toString()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun commitEdit() {
        val formatter = formatter
        if (formatter != null) {
            value = formatter.stringToValue(text) as Path?
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any?) {
        super.setValue(value as Path?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): Path? {
        return super.getValue() as Path?
    }

    companion object {
        private val LOG = KotlinLogging.logger { }
    }
}

