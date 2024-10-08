package de.krall.spreadsheets.expression.parser

object Diagnostics {
    val UNEXPECTED_TOKEN = DiagnosticFactory0(Severity.ERROR, "unexpected token")
    val EXPECTED_EXPRESSION = DiagnosticFactory0(Severity.ERROR, "expected expression")
    val EXPECTED_CLOSING_PARENTHESIS = DiagnosticFactory0(Severity.WARNING, "expected )")
}
