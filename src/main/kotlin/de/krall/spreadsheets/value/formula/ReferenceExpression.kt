package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.Reference

class ReferenceExpression(val reference: Reference) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Reference(reference)
    }
}
