package de.krall.spreadsheets.expression

import java.math.BigDecimal

sealed class Value {
    data class String(val text: kotlin.String) : Value()
    data class Number(val value: BigDecimal) : Value()
    data class Formula(val formula: de.krall.spreadsheets.expression.Formula) : Value()
}

sealed class Formula {

}

