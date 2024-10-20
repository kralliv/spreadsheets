package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

interface Function {

    fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue
}
