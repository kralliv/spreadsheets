package de.krall.spreadsheets.value

sealed class ParsedValue {
    data class Text(val text: String) : ParsedValue()
    data class Number(val number: Double) : ParsedValue()
    data class Formula(val formula: de.krall.spreadsheets.value.formula.Formula) : ParsedValue()
    data object BadFormula : ParsedValue()
}
