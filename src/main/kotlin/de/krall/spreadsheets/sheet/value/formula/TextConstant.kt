package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue

class TextConstant(val text: String) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue {
        return ComputedValue.Text(text)
    }
}
