package de.krall.spreadsheets.ui

import fernice.reflare.CSSEngine
import fernice.reflare.FlareLookAndFeel
import fernice.reflare.Stylesheet
import javax.swing.ToolTipManager

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
    if (OS.isMac) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    FlareLookAndFeel.install()

    CSSEngine.addStylesheet(Stylesheet.fromResource("/spreadsheets/css/main.css"))

    when {
        OS.isMac -> CSSEngine.addStylesheet(Stylesheet.fromResource("/spreadsheets/css/main-macos.css"))
    }
}

private fun configureComponents() {
    ToolTipManager.sharedInstance().dismissDelay = 10_000_000
}
