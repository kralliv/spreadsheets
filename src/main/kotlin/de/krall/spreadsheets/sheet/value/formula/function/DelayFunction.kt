package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

object DelayFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 1)

        val delay = number(arguments[0].dereference(references)) ?: return arguments[0]

        Thread.sleep(delay.toLong())

        return ComputedValue.Number(delay)
    }

    override fun toString(): String = "DELAY"
}
