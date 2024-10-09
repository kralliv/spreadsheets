package de.krall.spreadsheets.language.parser

import de.krall.spreadsheets.language.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.language.parser.diagnotic.DiagnosticCollector
import de.krall.spreadsheets.language.parser.diagnotic.DiagnosticSink
import de.krall.spreadsheets.language.parser.diagnotic.Severity

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