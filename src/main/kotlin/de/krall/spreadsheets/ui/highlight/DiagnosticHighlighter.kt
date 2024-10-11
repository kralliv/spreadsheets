package de.krall.spreadsheets.ui.highlight

import de.krall.spreadsheets.value.parser.SlSource
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.value.parser.diagnotic.Severity
import java.awt.Color
import java.awt.Graphics
import java.awt.Shape
import javax.swing.text.DefaultHighlighter
import javax.swing.text.Highlighter
import javax.swing.text.JTextComponent
import javax.swing.text.View
import kotlin.math.max
import kotlin.math.min

private val DEFAULT_PAINTER = StraightUnderlineHighlightPainter(Color.BLUE)
private val PAINTERS = mapOf(
    Severity.ERROR to StraightUnderlineHighlightPainter(Color.RED),
    Severity.WARNING to BackgroundHighlightPainter(Color(246, 190, 46))
)

class DiagnosticHighlighter : DefaultHighlighter() {

    private val diagnosticHighlights = mutableListOf<DiagnosticHighlight>()

    fun set(diagnostics: List<Diagnostic>) {
        diagnosticHighlights.clear()
        for (diagnostic in diagnostics) {
            val source = diagnostic.source ?: continue

            diagnosticHighlights.add(DiagnosticHighlight(diagnostic, source))
        }
    }

    fun clear() {
        diagnosticHighlights.clear()
    }

    fun getDiagnostics(position: Int): List<Diagnostic> {
        return diagnosticHighlights.filter { position in it.startOffset..it.endOffset }.map { it.diagnostic }
    }

    override fun paintLayeredHighlights(g: Graphics, p0: Int, p1: Int, viewBounds: Shape, editor: JTextComponent, view: View) {
        super.paintLayeredHighlights(g, p0, p1, viewBounds, editor, view)
        paintDiagnosticsHighlights(g, p0, p1, viewBounds, editor, view)
    }

    private fun paintDiagnosticsHighlights(g: Graphics, p0: Int, p1: Int, viewBounds: Shape, editor: JTextComponent, view: View) {
        for (highlight in diagnosticHighlights) {
            val start = highlight.startOffset
            val end = highlight.endOffset
            if (p0 < start && p1 >= start || p0 >= start && p0 < end) {
                highlight.paintLayeredHighlights(
                    g, p0, p1, viewBounds,
                    editor, view
                )
            }
        }
    }

    private class DiagnosticHighlight(val diagnostic: Diagnostic, val source: SlSource) : LayeredHighlight() {

        private val start = source.offset
        private val end = start + source.length

        override fun getStartOffset(): Int = start
        override fun getEndOffset(): Int = end

        override fun getPainter(): Highlighter.HighlightPainter = PAINTERS[diagnostic.severity] ?: DEFAULT_PAINTER
    }

    private abstract class LayeredHighlight : Highlighter.Highlight {

        fun paintLayeredHighlights(g: Graphics?, p0: Int, p1: Int, viewBounds: Shape?, editor: JTextComponent?, view: View?) {
            val start = max(this.startOffset, p0)
            val end = min(this.endOffset, p1)
            (this.painter as LayerPainter).paintLayer(g, start, end, viewBounds, editor, view)
        }
    }
}
