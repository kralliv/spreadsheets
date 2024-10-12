package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue

class TextConstant(val text: String) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Text(text)
    }
}
