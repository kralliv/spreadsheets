package de.krall.spreadsheets

import de.krall.spreadsheets.ui.DocumentWindow
import de.krall.spreadsheets.util.invokeLater
import fernice.reflare.FlareLookAndFeel

fun main() {
    FlareLookAndFeel.install()

    invokeLater {
        val window = DocumentWindow()

        window.isVisible = true
    }
}
