package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

object MultiplyFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        return ComputedValue.Number(left * right)
    }
}
