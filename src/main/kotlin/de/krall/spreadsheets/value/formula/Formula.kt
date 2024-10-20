package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.ReferenceRange
import de.krall.spreadsheets.value.Referencing

class Formula(val expression: Expression, val references: List<Referencing>) {

    fun compute(references: ReferenceResolver): ComputedValue {
        return expression.compute(references)
    }
}

interface ReferenceResolver {
    fun resolve(reference: Reference): ComputedValue
    fun resolve(referenceRange: ReferenceRange): Collection<ComputedValue>
}
