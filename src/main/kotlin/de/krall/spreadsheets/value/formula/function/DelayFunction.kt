package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue

object DelayFunction : UnaryOperatorFunction() {

    override fun compute(value: Double): ComputedValue? {
        Thread.sleep(value.toLong())

        return null
    }
}
