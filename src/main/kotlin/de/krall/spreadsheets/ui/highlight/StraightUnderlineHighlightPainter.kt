package de.krall.spreadsheets.ui.highlight

import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.Shape
import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent
import javax.swing.text.LayeredHighlighter
import javax.swing.text.Position.Bias
import javax.swing.text.View

// Derived from some example
class StraightUnderlineHighlightPainter(
    private val color: Color,
    private val thickness: Int = 2,
) : LayeredHighlighter.LayerPainter() {

    override fun paint(g: Graphics, p0: Int, p1: Int, bounds: Shape, c: JTextComponent) {}

    override fun paintLayer(g: Graphics, offs0: Int, offs1: Int, bounds: Shape, c: JTextComponent, view: View): Shape? {
        g.color = color
        return if (offs0 == view.startOffset && offs1 == view.endOffset) {
            val alloc = if (bounds is Rectangle) bounds else bounds.bounds
            paintUnderline(g, alloc, empty = offs0 == offs1)
            alloc
        } else {
            try {
                val shape = view.modelToView(offs0, Bias.Forward, offs1, Bias.Backward, bounds)
                val r = if (shape is Rectangle) shape else shape.bounds
                paintUnderline(g, r, empty = offs0 == offs1)
                r
            } catch (var9: BadLocationException) {
                var9.printStackTrace()
                null
            }
        }
    }

    private fun paintUnderline(g: Graphics, r: Rectangle, empty: Boolean) {
        val x = r.x - if (empty) 2 else 0
        val y = r.y + r.height - thickness
        val width = r.width + if (empty) 4 else 0
        g.fillRect(x, y, width, thickness)
    }
}
