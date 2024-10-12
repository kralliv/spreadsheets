package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

abstract class AbstractFunction : Function {

    protected fun resolve(value: ComputedValue?, references: ReferenceResolver): ComputedValue? {
        return when (value) {
            is ComputedValue.Reference -> references.resolve(value.reference)
            is ComputedValue.ReferenceRange -> error("illegal argument: reference-range")
            else -> value
        }
    }

    protected fun resolveAll(value: ComputedValue?, references: ReferenceResolver): Collection<ComputedValue> {
        return when (value) {
            is ComputedValue.Reference -> listOfNotNull(references.resolve(value.reference))
            is ComputedValue.ReferenceRange -> references.resolve(value.referenceRange)
            else -> listOfNotNull(value)
        }
    }
}
