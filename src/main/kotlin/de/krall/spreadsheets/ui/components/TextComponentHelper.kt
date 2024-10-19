package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.layout.AbstractLayout
import de.krall.spreadsheets.ui.util.marginBorderInsets
import de.krall.spreadsheets.ui.util.paddingInsets
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.text.JTextComponent

class TextComponentHelper(private val textComponent: JTextComponent) {

    private val documentListener = object : DocumentListener {
        override fun insertUpdate(event: DocumentEvent) = textChanged()
        override fun removeUpdate(event: DocumentEvent) = textChanged()
        override fun changedUpdate(event: DocumentEvent) = textChanged()
    }

    init {
        textComponent.layout = TextComponentLayout()
        textComponent.addPropertyChangeListener { event ->
            when (event.propertyName) {
                "document" -> documentChanged(event.oldValue as Document?, event.newValue as Document?)
                "enabled", "editable" -> updateComponentState()
            }
        }

        installKeyboardActions()

        documentChanged(null, textComponent.document)
    }

    private fun installKeyboardActions() {
        if (textComponent is JTextField) {
            textComponent.registerKeyboardAction(KeyStroke("UP")) {
                textComponent.caretPosition = 0
            }
            textComponent.registerKeyboardAction(KeyStroke("DOWN")) {
                textComponent.caretPosition = textComponent.text.length
            }
        }
    }

    private fun documentChanged(oldDocument: Document?, newDocument: Document?) {
        oldDocument?.removeDocumentListener(documentListener)
        newDocument?.addDocumentListener(documentListener)

        textChanged()
    }

    private var previousText = ""

    private fun textChanged() {
        val text = textComponent.text
        if (text != previousText) {
            previousText = text

            val event = TextChangeEvent(textComponent, previousText, text)
            textComponent.getListeners(TextChangeListener::class.java).forEach { it.textChanged(event) }
        }
    }

    var trailingComponent: Component? = null
        set(trailingComponent) {
            val previousTrailingComponent = field
            if (trailingComponent !== previousTrailingComponent) {
                if (previousTrailingComponent != null) {
                    previousTrailingComponent.isEnabled = true
                    textComponent.remove(previousTrailingComponent)
                }

                field = trailingComponent

                if (trailingComponent != null) {
                    textComponent.add(trailingComponent)
                    updateComponentState()
                }
            }
        }

    private fun updateComponentState() {
        trailingComponent?.let { component ->
            component.isEnabled = textComponent.isEnabled
        }
    }

    private inner class TextComponentLayout : AbstractLayout() {

        override fun preferredLayoutSize(target: Container): Dimension = error("text component should not rely on layout for its size")

        override fun layoutContainer(target: Container) {
            val size = target.size

            val borderInsets = target.marginBorderInsets
            val paddingInsets = target.paddingInsets

            val trailingComponent = trailingComponent
            if (trailingComponent != null && trailingComponent.isVisible) {
                val preferredSize = trailingComponent.preferredSize

                val x = size.width - borderInsets.right - ((paddingInsets.right) / 2) - preferredSize.width

                val maxHeight = size.height - borderInsets.top - borderInsets.bottom
                val height = preferredSize.height.coerceAtMost(maxHeight)
                val y = (maxHeight - height) / 2

                trailingComponent.setBounds(x, borderInsets.top + y, preferredSize.width, height)
            }
        }
    }
}