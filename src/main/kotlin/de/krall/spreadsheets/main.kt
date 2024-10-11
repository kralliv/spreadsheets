package de.krall.spreadsheets

import de.krall.spreadsheets.ui.DocumentWindow
import de.krall.spreadsheets.util.invokeLater
import fernice.reflare.CSSEngine
import fernice.reflare.FlareLookAndFeel
import fernice.reflare.Stylesheet

fun main() {
    invokeLater {
        initializeGraphicalEnvironment()

        val window = DocumentWindow()

        window.isVisible = true
    }
}

private fun initializeGraphicalEnvironment() {
    FlareLookAndFeel.install()

    CSSEngine.addStylesheet(Stylesheet.fromResource("/spreadsheets/css/main.css"))
}
