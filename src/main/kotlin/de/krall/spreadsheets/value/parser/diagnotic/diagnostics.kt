package de.krall.spreadsheets.value.parser.diagnotic

object Diagnostics {
    val UNEXPECTED_TOKEN = DiagnosticFactory0("UNEXPECTED_TOKEN", Severity.ERROR, "Unexpected token")
    val EXPECTED_EXPRESSION = DiagnosticFactory0("EXPECTED_EXPRESSION", Severity.ERROR, "Expected expression")
    val EXPECTED_CLOSING_PARENTHESIS = DiagnosticFactory0("EXPECTED_CLOSING_PARENTHESIS", Severity.WARNING, "Expected ')'")

    val UNKNOWN_FUNCTION = DiagnosticFactory1<String>("UNKNOWN_FUNCTION", Severity.ERROR, "Unknown function '{}'")
}
