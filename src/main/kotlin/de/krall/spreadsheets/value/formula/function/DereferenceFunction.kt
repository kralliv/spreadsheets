package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

object DereferenceFunction : AbstractFunction() {

    override fun call(
        arguments: List<ComputedValue>,
        references: ReferenceResolver,
    ): ComputedValue {
        assert(arguments.size == 1)

        return arguments[0].dereference(references)
    }
}
