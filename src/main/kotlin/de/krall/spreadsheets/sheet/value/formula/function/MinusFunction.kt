package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue

object MinusFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        return ComputedValue.Number(left - right)
    }

    override fun toString(): String = "MINUS"
}
