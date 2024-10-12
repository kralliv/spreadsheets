package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputationError
import de.krall.spreadsheets.value.ComputedValue

object DivideFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        if (right == 0.0) return ComputedValue.Error(ComputationError.DivisionByZero)
        return ComputedValue.Number(left / right)
    }
}
