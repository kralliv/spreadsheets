package de.krall.spreadsheets.ui.util

import javax.swing.SwingUtilities

inline fun invokeLater(crossinline block: () -> Unit) {
    SwingUtilities.invokeLater { block() }
}
