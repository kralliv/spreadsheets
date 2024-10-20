package de.krall.spreadsheets.sheet.value

sealed class EvaluatedValue {
    data class Text(val text: String) : EvaluatedValue()
    data class Number(val number: Double) : EvaluatedValue()
    data class Error(val error: ComputationError) : EvaluatedValue()
    data object Unevaluated : EvaluatedValue()
}
