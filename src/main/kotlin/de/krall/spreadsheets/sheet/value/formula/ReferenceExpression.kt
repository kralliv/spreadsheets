package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.Reference

class ReferenceExpression(val reference: Reference) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Reference(reference)
    }
}
