package de.krall.spreadsheets.language

import java.math.BigDecimal

sealed class Value {
    data class String(val text: kotlin.String) : Value()
    data class Number(val value: BigDecimal) : Value()
    data class Formula(val formula: de.krall.spreadsheets.language.Formula) : Value()
}

sealed class Formula {

}

