package de.krall.spreadsheets.value

import de.krall.spreadsheets.value.parser.tree.SlExpression
import java.math.BigDecimal

sealed class ParsedValue {
    data class Text(val text: String) : ParsedValue()
    data class Number(val number: BigDecimal) : ParsedValue()
    data class Formula(val formula: SlExpression) : ParsedValue()
    data object BadFormula : ParsedValue()
}


