package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

abstract class BinaryOperatorFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        assert(arguments.size == 2)

        val left = when (val value = resolve(arguments[0], references)) {
            null -> 0.0
            is ComputedValue.Text -> 0.0
            is ComputedValue.Number -> value.number
            is ComputedValue.Reference -> 0.0
            is ComputedValue.ReferenceRange -> 0.0
            is ComputedValue.Error -> return value
        }

        val right = when (val value = resolve(arguments[1], references)) {
            null -> 0.0
            is ComputedValue.Text -> 0.0
            is ComputedValue.Number -> value.number
            is ComputedValue.Reference -> 0.0
            is ComputedValue.ReferenceRange -> 0.0
            is ComputedValue.Error -> return value
        }

        return compute(left, right)
    }

    protected abstract fun compute(left: Double, right: Double): ComputedValue
}