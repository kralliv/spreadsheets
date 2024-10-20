package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputationError
import de.krall.spreadsheets.sheet.value.ComputedValue

object ModuloFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        if (right == 0.0) return ComputedValue.Error(ComputationError.DivisionByZero)
        return ComputedValue.Number(left % right)
    }

    override fun toString(): String = "MODULO"
}
