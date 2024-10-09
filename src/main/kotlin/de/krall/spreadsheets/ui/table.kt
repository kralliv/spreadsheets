package de.krall.spreadsheets.ui

import org.jdesktop.swingx.JXTable
import javax.swing.JTable

class SpreadsheetTable : JXTable() {

    init {
        isHorizontalScrollEnabled = true
        border = null
    }
}
