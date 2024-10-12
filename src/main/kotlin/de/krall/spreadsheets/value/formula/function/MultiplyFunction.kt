package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

object MultiplyFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        return ComputedValue.Number(left * right)
    }
}
