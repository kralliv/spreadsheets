package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange
import de.krall.spreadsheets.sheet.value.Referencing

class Formula(val expression: Expression) {

    fun compute(references: ReferenceResolver): ComputedValue {
        return expression.compute(references)
    }
}
