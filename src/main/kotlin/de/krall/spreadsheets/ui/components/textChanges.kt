package de.krall.spreadsheets.ui.components

import java.util.EventListener
import java.util.EventObject
import javax.swing.text.JTextComponent

fun interface TextChangeListener : EventListener {
    fun textChanged(event: TextChangeEvent)
}

class TextChangeEvent(source: JTextComponent, val oldText: String, val newText: String) : EventObject(source) {

    override fun getSource(): JTextComponent = super.getSource() as JTextComponent

    override fun toString(): String {
        return "TextChangeEvent[oldText='$oldText', newText='$newText', source=$source]"
    }
}
