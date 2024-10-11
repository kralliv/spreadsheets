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
class BackgroundHighlightPainter(
    private val color: Color,
) : LayeredHighlighter.LayerPainter() {

    override fun paint(g: Graphics, p0: Int, p1: Int, bounds: Shape, c: JTextComponent) {}

    override fun paintLayer(g: Graphics, offs0: Int, offs1: Int, bounds: Shape, c: JTextComponent, view: View): Shape? {
        g.color = color
        return if (offs0 == view.startOffset && offs1 == view.endOffset) {
            val alloc = if (bounds is Rectangle) bounds else bounds.bounds
            paintBackground(g, alloc)
            alloc
        } else {
            try {
                val shape = view.modelToView(offs0, Bias.Forward, offs1, Bias.Backward, bounds)
                val r = if (shape is Rectangle) shape else shape.bounds
                if (offs0 == offs1) {
                    r.width += 2
                }
                paintBackground(g, r)
                r
            } catch (var9: BadLocationException) {
                var9.printStackTrace()
                null
            }
        }
    }

    private fun paintBackground(g: Graphics, r: Rectangle) {
        val x = r.x
        val y = r.y
        g.fillRect(x, y, r.width, r.height)
    }
}