package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange

interface ReferenceResolver {
    fun resolve(reference: Reference): ComputedValue
    fun resolve(referenceRange: ReferenceRange): Collection<ComputedValue>
}
