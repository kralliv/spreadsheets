package de.krall.spreadsheets.sheet.value.parser.diagnotic

import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.util.messageFormat

abstract class DiagnosticFactory {
    abstract val name: String
}

class DiagnosticFactory0(override val name: String, val severity: Severity, val template: String) : DiagnosticFactory() {

    fun on(element: SlElement): Diagnostic {
        return Diagnostic(this, severity, template, element)
    }
}

class DiagnosticFactory1<A>(override val name: String, val severity: Severity, val template: String) : DiagnosticFactory() {

    fun on(element: SlElement, first: A): Diagnostic {
        return Diagnostic(this, severity, template.messageFormat(first), element)
    }
}

class DiagnosticFactory2<A, B>(override val name: String, val severity: Severity, val template: String) : DiagnosticFactory() {

    fun on(element: SlElement, first: A, second: B): Diagnostic {
        return Diagnostic(this, severity, template.messageFormat(first, second), element)
    }
}
