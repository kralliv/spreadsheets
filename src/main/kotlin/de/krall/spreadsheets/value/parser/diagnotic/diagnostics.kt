package de.krall.spreadsheets.value.parser.diagnotic

import de.krall.spreadsheets.value.parser.type.Type

object Diagnostics {
    val UNEXPECTED_TOKEN = DiagnosticFactory0("UNEXPECTED_TOKEN", Severity.ERROR, "Unexpected token")
    val EXPECTED_EXPRESSION = DiagnosticFactory0("EXPECTED_EXPRESSION", Severity.ERROR, "Expected expression")
    val EXPECTED_REFERENCE = DiagnosticFactory0("EXPECTED_REFERENCE", Severity.ERROR, "Expected reference")
    val EXPECTED_CLOSING_PARENTHESIS = DiagnosticFactory0("EXPECTED_CLOSING_PARENTHESIS", Severity.WARNING, "Expected ')'")

    val INVALID_REFERENCE = DiagnosticFactory1<String>("INVALID_REFERENCE", Severity.ERROR, "Invalid reference '{}'")
    val INVALID_REFERENCE_RANGE = DiagnosticFactory1<String>("INVALID_REFERENCE_RANGE", Severity.ERROR, "Invalid reference range '{}'")

    val TYPE_MISMATCH = DiagnosticFactory2<Type, Type>("TYPE_MISMATCH", Severity.ERROR, "Type mismatch: expected '{}' but found '{}'")

    val UNKNOWN_FUNCTION = DiagnosticFactory1<String>("UNKNOWN_FUNCTION", Severity.ERROR, "Unknown function '{}'")
    val MISSING_FUNCTION_ARGUMENT = DiagnosticFactory1<Type>("MISSING_FUNCTION_ARGUMENT", Severity.ERROR, "No value passed for argument of type '{}'")
}
