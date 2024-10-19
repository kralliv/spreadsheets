package de.krall.spreadsheets.ui.components

interface STextComponent {

    // Kotlin does not allow overriding Java function with properties
    // var text: String

    fun addTextChangeListener(listener: TextChangeListener)
    fun removeTextChangeListener(listener: TextChangeListener)
}