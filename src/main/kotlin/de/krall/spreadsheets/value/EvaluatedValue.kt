package de.krall.spreadsheets.value

import java.math.BigDecimal

sealed class EvaluatedValue {
    data class Text(val text: String) : EvaluatedValue()
    data class Number(val number: BigDecimal) : EvaluatedValue()
    data object Unevaluated : EvaluatedValue()
    data object BadFormula : EvaluatedValue()
    data object CircularDependencies : EvaluatedValue()
}
