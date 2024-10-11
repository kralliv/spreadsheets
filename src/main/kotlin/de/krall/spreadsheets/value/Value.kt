package de.krall.spreadsheets.value

import java.math.BigDecimal

sealed class Value {
    data class Text(val text: String) : Value()
    data class Number(val value: BigDecimal) : Value()
    data class Formula(val formula: String) : Value()
}
