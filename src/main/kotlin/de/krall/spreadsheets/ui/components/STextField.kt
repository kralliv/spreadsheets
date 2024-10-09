package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.util.add
import de.krall.spreadsheets.util.forEach
import de.krall.spreadsheets.util.remove
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

open class STextField : JTextField() {

    init {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent) = textChanged()
            override fun removeUpdate(event: DocumentEvent) = textChanged()
            override fun changedUpdate(event: DocumentEvent) = textChanged()

            private var previousText = ""

            private fun textChanged() {
                val text = text
                if (text != previousText) {
                    previousText = text

                    val event = TextChangeEvent(this@STextField, previousText, text)
                    listenerList.forEach<TextChangeListener> { it.textChanged(event) }
                }
            }
        })
    }

    fun addTextChangeListener(listener: TextChangeListener) = listenerList.add(listener)
    fun removeTextChangeListener(listener: TextChangeListener) = listenerList.remove(listener)
}
