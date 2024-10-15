package de.krall.spreadsheets.ui.event

import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

val MouseEvent.isLeftButton: Boolean
    get() = SwingUtilities.isLeftMouseButton(this)

val MouseEvent.isRightButton: Boolean
    get() = SwingUtilities.isRightMouseButton(this)
