package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue

object NegateFunction : UnaryOperatorFunction() {

    override fun compute(value: Double): ComputedValue {
        return ComputedValue.Number(-value)
    }
}
