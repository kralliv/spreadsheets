package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue

class NumberConstant(val number: Double) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Number(number)
    }
}
