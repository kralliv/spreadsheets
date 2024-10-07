package de.krall.spreadsheets.expression.parser

class Diagnostic(val message: String, val offset: Int, val length: Int)

fun interface DiagnosticSink {
    fun report(diagnostic: Diagnostic)
}
