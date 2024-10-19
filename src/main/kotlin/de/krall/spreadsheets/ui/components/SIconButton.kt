package de.krall.spreadsheets.ui.components

import fernice.reflare.classes
import javax.swing.Icon
import javax.swing.JButton

class SIconButton(icon: Icon): JButton(icon) {

    init {
        isFocusable = false
        classes.add("s-icon-button")
    }
}
