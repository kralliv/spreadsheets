package de.krall.spreadsheets.ui

import de.krall.spreadsheets.value.parser.ValueParser
import de.krall.spreadsheets.ui.components.STextField
import de.krall.spreadsheets.ui.highlight.DiagnosticHighlighter
import java.awt.event.MouseEvent
import javax.swing.ToolTipManager
import kotlin.time.Duration.Companion.nanoseconds

class ValueField(val parser: ValueParser) : STextField() {

    private val diagnosticHighlighter = DiagnosticHighlighter()

    init {
        columns = 20
        highlighter = diagnosticHighlighter

        addTextChangeListener {
            val (_, diagnostics) = parser.parseParsedValueTree(text)

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
