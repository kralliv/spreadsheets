package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import javax.swing.text.DefaultFormatterFactory

class SIntField : SFormattedTextField() {

    init {
        formatterFactory = DefaultFormatterFactory(IntFormatter())

        installKeyboardActions()
    }

    private fun installKeyboardActions() {
        registerKeyboardAction(KeyStroke("ENTER")) {
            commitEdit()

            fireActionPerformed()
        }
    }

    private inner class IntFormatter : AbstractFormatter() {

        override fun stringToValue(text: String?): Any? {
            if (text.isNullOrEmpty()) return null
            return try {
                text.toIntOrNull()
            } catch (ignore: Exception) {
                null
            }
        }

        override fun valueToString(value: Any?): String {
            if (value == null || value !is Int) return ""
            return value.toString()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun commitEdit() {
        val formatter = formatter
        if (formatter != null) {
            value = formatter.stringToValue(text) as Int?
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any?) {
        super.setValue(value as Int?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): Int? {
        return super.getValue() as Int?
    }
}