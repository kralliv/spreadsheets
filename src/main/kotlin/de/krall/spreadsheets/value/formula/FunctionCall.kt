package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.function.Function

class FunctionCall(val function: Function, val arguments: List<Expression>) : Expression {

    override fun compute(references: ReferenceResolver): ComputedValue? {
        val values = arguments.map { it.compute(references) }

        return function.call(values, references)
    }
}
