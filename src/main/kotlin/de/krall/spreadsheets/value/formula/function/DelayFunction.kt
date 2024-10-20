package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

object DelayFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 1)

        val delay = number(arguments[0].dereference(references)) ?: return arguments[0]

        Thread.sleep(delay.toLong())

        return ComputedValue.Blank
    }
}
