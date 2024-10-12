package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.ComputedValue

class Formula(val expression: Expression, val references: List<Reference>) {

    fun compute(references: ReferenceResolver): ComputedValue? {
        return expression.compute(references)
    }
}

interface ReferenceResolver {
    fun resolve(cell: Reference.Cell): ComputedValue?
    fun resolve(area: Reference.Area): Collection<ComputedValue>

    fun resolveAll(area: Reference): Collection<ComputedValue>
}
