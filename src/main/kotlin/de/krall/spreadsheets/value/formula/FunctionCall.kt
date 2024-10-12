package de.krall.spreadsheets.value.formula

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.function.Function
import de.krall.spreadsheets.value.parser.type.FunctionDefinition

class FunctionCall(val function: Function, val arguments: List<Argument>) : Expression {

    var definition: FunctionDefinition? = null

    override fun compute(references: ReferenceResolver): ComputedValue {
        return function.call(arguments, references)
    }
}

sealed class Argument {
    data class Expression(val expression: de.krall.spreadsheets.value.formula.Expression) : Argument()
    data class Reference(val reference: de.krall.spreadsheets.value.Reference) : Argument()
}
