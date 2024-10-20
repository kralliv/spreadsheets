package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue

class NumberConstant(val number: Double) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Number(number)
    }
}
