package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

abstract class BinaryOperatorFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 2)

        val left = number(arguments[0].dereference(references)) ?: return arguments[0]
        val right = number(arguments[1].dereference(references)) ?: return arguments[1]

        return compute(left, right)
    }

    protected abstract fun compute(left: Double, right: Double): ComputedValue
}
