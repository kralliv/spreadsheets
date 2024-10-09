package de.krall.spreadsheets.language.parser.diagnotic

import de.krall.spreadsheets.language.parser.tree.SlElement
import de.krall.spreadsheets.util.messageFormat

abstract class DiagnosticFactory {
    abstract val name: String
}

class DiagnosticFactory0(override val name: String, val severity: Severity, val template: String) : DiagnosticFactory() {

    fun on(element: SlElement): Diagnostic {
        return Diagnostic(this, severity, template, element)
    }
}

class DiagnosticFactory1<T>(override val name: String, val severity: Severity, val template: String) : DiagnosticFactory() {

    fun on(element: SlElement, first: T): Diagnostic {
        return Diagnostic(this, severity, template.messageFormat(first), element)
    }
}
