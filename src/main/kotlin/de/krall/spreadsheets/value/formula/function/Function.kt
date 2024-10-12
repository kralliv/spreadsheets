package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

interface Function {

    fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue
}
