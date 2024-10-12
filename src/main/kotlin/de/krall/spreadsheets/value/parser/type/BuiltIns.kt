package de.krall.spreadsheets.value.parser.type

import de.krall.spreadsheets.value.formula.function.AddFunction
import de.krall.spreadsheets.value.formula.function.DivideFunction
import de.krall.spreadsheets.value.formula.function.MinusFunction
import de.krall.spreadsheets.value.formula.function.MultiplyFunction
import de.krall.spreadsheets.value.formula.function.SumFunction

object BuiltIns {

    val Any = AnyType
    val String = StringType
    val Number = NumberType
    val Reference = ReferenceType
    val ReferenceRange = ReferenceRangeType
    val Nothing = NothingType

    private val NonRange = UnionType(String, Number, Reference)

    val functions = listOf(
        FunctionDefinition("sum", variadic(Any), Number, SumFunction),
        FunctionDefinition("add", fixed(NonRange, NonRange), Number, AddFunction),
        FunctionDefinition("minus", fixed(NonRange, NonRange), Number, MinusFunction),
        FunctionDefinition("multiply", fixed(NonRange, NonRange), Number, MultiplyFunction),
        FunctionDefinition("divide", fixed(NonRange, NonRange), Number, DivideFunction),
    )
}
