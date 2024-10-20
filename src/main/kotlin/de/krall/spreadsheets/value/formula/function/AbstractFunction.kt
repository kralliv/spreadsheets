package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

abstract class AbstractFunction : Function {

    protected fun number(value: ComputedValue): Double? {
        return when (value) {
            is ComputedValue.Blank -> 0.0
            is ComputedValue.Text -> 0.0
            is ComputedValue.Number -> value.number
            is ComputedValue.Reference -> 0.0
            is ComputedValue.ReferenceRange -> 0.0
            is ComputedValue.Error -> null
        }
    }

    protected fun text(value: ComputedValue): String? {
        return when (value) {
            is ComputedValue.Blank -> ""
            is ComputedValue.Text -> value.text
            is ComputedValue.Number -> value.number.toString()
            is ComputedValue.Reference -> value.reference.toString()
            is ComputedValue.ReferenceRange -> value.referenceRange.toString()
            is ComputedValue.Error -> null
        }
    }

    protected fun ComputedValue.dereference(references: ReferenceResolver): ComputedValue {
        return when (this) {
            is ComputedValue.Reference -> references.resolve(this.reference)
            is ComputedValue.ReferenceRange -> error("illegal argument: reference-range")
            else -> this
        }
    }

    protected fun ComputedValue.dereferenceAll(references: ReferenceResolver): Collection<ComputedValue> {
        return when (this) {
            is ComputedValue.Reference -> listOfNotNull(references.resolve(this.reference))
            is ComputedValue.ReferenceRange -> references.resolve(this.referenceRange)
            else -> listOfNotNull(this)
        }
    }
}
