package de.krall.spreadsheets.value.parser

import de.krall.spreadsheets.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.value.parser.diagnotic.DiagnosticCollector
import de.krall.spreadsheets.value.parser.diagnotic.DiagnosticSink

class ProcessingContext : DiagnosticSink {

    private val diagnosticCollector = DiagnosticCollector()

    val diagnostics: List<Diagnostic>
        get() = diagnosticCollector.toList()

    fun hasWarnings() = diagnosticCollector.hasWarnings()
    fun hasErrors() = diagnosticCollector.hasErrors()

    override fun report(diagnostic: Diagnostic) {
        diagnosticCollector.report(diagnostic)
    }
}