package de.krall.spreadsheets.ui

import de.krall.spreadsheets.value.parser.ValueParser
import de.krall.spreadsheets.ui.components.SFormattedTextField
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.highlight.DiagnosticHighlighter
import de.krall.spreadsheets.value.Value
import java.awt.event.MouseEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.swing.ToolTipManager
import javax.swing.text.DefaultFormatterFactory

class ValueField(val parser: ValueParser) : SFormattedTextField() {

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

    init {
        formatterFactory = DefaultFormatterFactory(FormatterImpl())

        registerKeyboardAction(KeyStroke("ENTER")) {
            commitEdit()

            fireActionPerformed()
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

    private inner class FormatterImpl : AbstractFormatter() {

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
                exception.printStackTrace() // TODO
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

    override fun getToolTipText(event: MouseEvent): String? {
        val position = viewToModel2D(event.point)
        val highlights = diagnosticHighlighter.getDiagnostics(position)

        if (highlights.isNotEmpty()) {
            return highlights.first().message
        }

        return null
    }
}
