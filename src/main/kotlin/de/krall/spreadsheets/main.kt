package de.krall.spreadsheets

import de.krall.spreadsheets.ui.DocumentWindow
import de.krall.spreadsheets.ui.initializeGraphicalEnvironment
import de.krall.spreadsheets.ui.event.invokeLater

fun main() {
    invokeLater {
        initializeGraphicalEnvironment()

        val window = DocumentWindow()

        window.isVisible = true
    }
}

