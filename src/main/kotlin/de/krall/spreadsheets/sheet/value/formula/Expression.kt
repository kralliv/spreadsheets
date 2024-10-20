package de.krall.spreadsheets.sheet.value.formula

import de.krall.spreadsheets.sheet.value.ComputedValue

sealed interface Expression {

    fun compute(references: ReferenceResolver): ComputedValue
}
