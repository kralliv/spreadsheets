package de.krall.spreadsheets.language.parser.diagnotic

import de.krall.spreadsheets.language.parser.SlSource
import de.krall.spreadsheets.language.parser.tree.SlElement

class Diagnostic(val factory: DiagnosticFactory, val severity: Severity, val message: String, val element: SlElement) {

    val name: String
        get() = factory.name

    val source: SlSource?
        get() = element.source

    override fun toString(): String = buildString {
        append(severity)
        append(": ")
        append(message)
        source?.let { source ->
            append(" at position ")
            append(source.offset)
            append(" '")
            append(source.text)
            append("'")
        }
    }
}

enum class Severity {
    WARNING,
    ERROR,
}





