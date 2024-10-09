package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.language.parser.LanguageProcessor
import de.krall.spreadsheets.ui.input.highlight.DiagnosticHighlighter
import java.awt.event.MouseEvent
import java.util.Locale
import javax.swing.ToolTipManager

class LanguageTextField : STextField() {

    private val diagnosticHighlighter = DiagnosticHighlighter()

    init {
        columns = 20
        highlighter = diagnosticHighlighter

        val processor = LanguageProcessor()

        addTextChangeListener {
            val (_, diagnostics) = processor.process(text)

            diagnosticHighlighter.set(diagnostics)
            repaint()
        }

        val toolTipManager = ToolTipManager.sharedInstance()
        toolTipManager.registerComponent(this)
    }

    override fun getToolTipText(event: MouseEvent): String? {
        val position = viewToModel2D(event.point)
        val highlights = diagnosticHighlighter.getDiagnostics(position)

        if (highlights.isNotEmpty()) {
            return highlights.first().message
        }

        return null
    }
}
