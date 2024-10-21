@file:JvmName("Spreadsheets")
package de.krall.spreadsheets

import de.krall.spreadsheets.ui.SpreadsheetManager
import de.krall.spreadsheets.ui.env.initializeGraphicalEnvironment
import de.krall.spreadsheets.ui.util.invokeLater

fun main() {
    invokeLater {
        initializeGraphicalEnvironment()

        SpreadsheetManager().initialize()
    }
}
