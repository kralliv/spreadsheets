package de.krall.spreadsheets.ui.util

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import kotlin.math.abs
import kotlin.math.min

// AWT has some bad values for their bias causing lines to
// be shifted by a subpixel on higher density displays.
internal fun Graphics.drawAALine(x1: Int, y1: Int, x2: Int, y2: Int) {
    assert(x1 == x2 || y1 == y2)
    if (y1 == y2) {
        val x = min(x1, x2)
        val width = abs(x2 - x1) + 1
        fillRect(x, y1, width, 1)
    } else if (x1 == x2) {
        val y = min(y1, y2)
        val height = abs(y2 - y1) + 1
        fillRect(x1, y, 1, height)
    } else {
        error("non axis-aligned line: ${x1}x${y1} to ${x2}x${y2}")
    }
}

internal inline fun Graphics.useGraphics2D(block: (Graphics2D) -> Unit) {
    val g2 = create() as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    block(g2)
}
