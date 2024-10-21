package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.components.SFormattedTextField
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.highlight.DiagnosticHighlighter
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Point
import java.awt.event.MouseEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.ToolTipManager
import javax.swing.text.DefaultFormatterFactory

open class ValueField(val parser: ValueParser) : SFormattedTextField() {

    private val diagnosticHighlighter = DiagnosticHighlighter()

    init {
        formatterFactory = DefaultFormatterFactory(ValueFormatter())

        val toolTipManager = ToolTipManager.sharedInstance()
        toolTipManager.registerComponent(this)

        installHighlighting()
        installKeyboardActions()
    }

    private fun installHighlighting() {
        highlighter = diagnosticHighlighter

        addTextChangeListener {
            val (_, diagnostics) = parser.parseParsedValueTree(text)

            diagnosticHighlighter.set(diagnostics)
            repaint()
        }
    }

    private fun installKeyboardActions() {
        registerKeyboardAction(KeyStroke("ENTER")) {
            commitEdit()

            fireActionPerformed()
        }
    }

    private inner class ValueFormatter : AbstractFormatter() {

        private val numberFormat = DecimalFormat().apply {
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ROOT)
            minimumIntegerDigits = 1
            minimumFractionDigits = 0
            maximumFractionDigits = Int.MAX_VALUE
        }

        override fun stringToValue(text: String?): Any? {
            if (text.isNullOrEmpty()) return null
            return try {
                parser.parseValue(text)
            } catch (exception: Exception) {
                LOG.error(exception) { "failed to convert text to value: '$text'" }
                null
            }
        }

        override fun valueToString(value: Any?): String {
            if (value == null || value !is Value) return ""
            return when (value) {
                is Value.Text -> value.text
                is Value.Number -> numberFormat.format(value.number)
                is Value.Formula -> "=${value.formula}"
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun commitEdit() {
        val formatter = formatter
        if (formatter != null) {
            value = formatter.stringToValue(text) as Value?
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any?) {
        super.setValue(value as Value?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(): Value? {
        return super.getValue() as Value?
    }

    override fun getToolTipText(event: MouseEvent): String? {
        val position = viewToModel2D(event.point)

        val diagnostics = diagnosticHighlighter.getDiagnostics(position)
        if (diagnostics.isEmpty()) return null

        return buildString {
            append("<html><body>")

            for ((index, diagnostic) in diagnostics.withIndex()) {
                if (index > 0) append("<br>")
                append(diagnostic.message)
            }

            append("</body></html>")
        }
    }

    override fun getToolTipLocation(event: MouseEvent): Point? {
        val position = viewToModel2D(event.point)

        val diagnostics = diagnosticHighlighter.getDiagnostics(position)
        if (diagnostics.isEmpty()) return null

        return Point(0, height)
    }

    companion object {
        private val LOG = KotlinLogging.logger { }
    }
}
