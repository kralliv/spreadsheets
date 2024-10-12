package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue

object AddFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        return ComputedValue.Number(left + right)
    }
}
