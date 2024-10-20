package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.ReferenceRange

class ReferenceRangeExpression(val referenceRange: ReferenceRange) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.ReferenceRange(referenceRange)
    }
}
