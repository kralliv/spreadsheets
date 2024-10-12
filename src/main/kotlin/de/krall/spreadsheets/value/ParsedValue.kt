package de.krall.spreadsheets.value

import de.krall.spreadsheets.value.parser.tree.SlExpression

sealed class ParsedValue {
    data class Text(val text: String) : ParsedValue()
    data class Number(val number: Double) : ParsedValue()
    data class Formula(val formula: SlExpression, val references: List<Reference>) : ParsedValue()
    data object BadFormula : ParsedValue()
}


