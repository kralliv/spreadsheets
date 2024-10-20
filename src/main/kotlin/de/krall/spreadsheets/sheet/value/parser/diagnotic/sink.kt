package de.krall.spreadsheets.sheet.value.parser.diagnotic

fun interface DiagnosticSink {
    fun report(diagnostic: Diagnostic)
}

class DiagnosticCollector : DiagnosticSink {

    private val diagnostics = mutableListOf<Diagnostic>()

    override fun report(diagnostic: Diagnostic) {
        diagnostics.add(diagnostic)
    }

    fun hasWarnings(): Boolean = diagnostics.any { it.severity >= Severity.WARNING }
    fun hasErrors(): Boolean = diagnostics.any { it.severity >= Severity.ERROR }

    fun toList(): List<Diagnostic> {
        return diagnostics.toList()
    }
}
