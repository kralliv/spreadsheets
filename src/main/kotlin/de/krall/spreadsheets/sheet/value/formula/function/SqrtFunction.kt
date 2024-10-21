package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import kotlin.math.sqrt

object SqrtFunction : UnaryOperatorFunction() {

    override fun compute(value: Double): ComputedValue {
        return ComputedValue.Number(sqrt(value))
    }

    override fun toString(): String = "SQRT"
}
