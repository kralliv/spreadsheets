package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.ReferenceRange

class ReferenceRangeExpression(val referenceRange: ReferenceRange) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.ReferenceRange(referenceRange)
    }
}
