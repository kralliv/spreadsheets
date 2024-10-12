package de.krall.spreadsheets.value

sealed class Value {
    data class Text(val text: String) : Value()
    data class Number(val number: Double) : Value()
    data class Formula(val formula: String) : Value()
}
