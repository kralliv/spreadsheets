package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue

object NegateFunction : UnaryOperatorFunction() {

    override fun compute(value: Double): ComputedValue {
        return ComputedValue.Number(-value)
    }

    override fun toString(): String = "NEGATE"
}
