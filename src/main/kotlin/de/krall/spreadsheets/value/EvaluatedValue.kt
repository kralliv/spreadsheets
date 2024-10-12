package de.krall.spreadsheets.value

sealed class EvaluatedValue {
    data class Text(val text: String) : EvaluatedValue()
    data class Number(val number: Double) : EvaluatedValue()
    data object Unevaluated : EvaluatedValue()
    data object BadFormula : EvaluatedValue()
    data object CircularDependencies : EvaluatedValue()
}
