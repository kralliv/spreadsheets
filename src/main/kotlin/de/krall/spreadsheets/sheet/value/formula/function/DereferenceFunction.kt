package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

object DereferenceFunction : AbstractFunction() {

    override fun call(
        arguments: List<ComputedValue>,
        references: ReferenceResolver,
    ): ComputedValue {
        assert(arguments.size == 1)

        return arguments[0].dereference(references)
    }

    override fun toString(): String = "DEREF"
}
