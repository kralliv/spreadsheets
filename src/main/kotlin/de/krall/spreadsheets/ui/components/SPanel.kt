package de.krall.spreadsheets.ui.components

import fernice.reflare.isFocusDismissible
import javax.swing.JPanel

open class SContainer : JPanel() {

    init {
        isFocusDismissible = false
    }
}
