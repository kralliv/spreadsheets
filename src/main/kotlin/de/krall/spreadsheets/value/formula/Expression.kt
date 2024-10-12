package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue

interface Expression {

    fun compute(references: ReferenceResolver): ComputedValue?
}
