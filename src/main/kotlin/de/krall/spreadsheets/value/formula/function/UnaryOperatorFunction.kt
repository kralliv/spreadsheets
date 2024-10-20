package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

abstract class UnaryOperatorFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 1)

        val only = number(arguments[0].dereference(references)) ?: return arguments[0]

        return compute(only)
    }

    protected abstract fun compute(value: Double): ComputedValue
}
