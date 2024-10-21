package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import kotlin.math.pow

object PowFunction : BinaryOperatorFunction() {

    override fun compute(left: Double, right: Double): ComputedValue {
        return ComputedValue.Number(left.pow(right))
    }

    override fun toString(): String = "POW"
}
