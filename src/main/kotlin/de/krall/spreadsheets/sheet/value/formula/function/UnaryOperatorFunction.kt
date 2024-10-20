package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

abstract class UnaryOperatorFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 1)

        val only = number(arguments[0].dereference(references)) ?: return arguments[0]

        return compute(only)
    }

    protected abstract fun compute(value: Double): ComputedValue
}
