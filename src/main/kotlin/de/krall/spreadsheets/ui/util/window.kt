package de.krall.spreadsheets.ui.util

import java.awt.Component
import java.awt.Window
import javax.swing.FocusManager
import javax.swing.SwingUtilities

val Component.window: Window?
    get() = when (this) {
        is Window -> this
        else -> SwingUtilities.getWindowAncestor(this)
    }

val carrierWindow: Window?
    get() {
        val focusManager = FocusManager.getCurrentManager()
        return focusManager.activeWindow
            ?: focusManager.focusedWindow
            ?: Window.getOwnerlessWindows().firstOrNull { it.isShowing }
    }
