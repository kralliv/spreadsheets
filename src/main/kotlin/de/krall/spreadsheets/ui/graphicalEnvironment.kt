package de.krall.spreadsheets.ui

import fernice.reflare.CSSEngine
import fernice.reflare.FlareLookAndFeel
import fernice.reflare.Stylesheet
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.InputMap
import javax.swing.KeyStroke
import javax.swing.ToolTipManager
import javax.swing.UIManager
import javax.swing.text.DefaultEditorKit

object OS {
    private val operatingSystem = (System.getProperty("spreadsheets.os") ?: System.getProperty("os.name")).lowercase()

    val isWindows = operatingSystem.contains("wind")
    val isLinux = operatingSystem.contains("linux")
    val isMac = operatingSystem.startsWith("mac")
}

fun initializeGraphicalEnvironment() {
    installLookAndFeel()
    configureComponents()
}

private fun installLookAndFeel() {
    FlareLookAndFeel.install()

    CSSEngine.addStylesheet(Stylesheet.fromResource("/spreadsheets/css/main.css"))
}

private fun configureComponents() {
    ToolTipManager.sharedInstance().dismissDelay = 10_000_000
}
