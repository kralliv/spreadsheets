package de.krall.spreadsheets.value.parser.type

import de.krall.spreadsheets.value.formula.function.AddFunction
import de.krall.spreadsheets.value.formula.function.DivideFunction
import de.krall.spreadsheets.value.formula.function.MinusFunction
import de.krall.spreadsheets.value.formula.function.ModuloFunction
import de.krall.spreadsheets.value.formula.function.MultiplyFunction
import de.krall.spreadsheets.value.formula.function.SumFunction

object BuiltIns {

    val Any = AnyType
    val Text = TextType
    val Number = NumberType
    val Reference = ReferenceType
    val ReferenceRange = ReferenceRangeType
    val Nothing = NothingType

    val AnySingular = UnionType(Text, Number, Reference)

    val functions = listOf(
        FunctionDefinition("sum", variadic(Any), Number, SumFunction),
        FunctionDefinition("add", fixed(AnySingular, AnySingular), Number, AddFunction),
        FunctionDefinition("minus", fixed(AnySingular, AnySingular), Number, MinusFunction),
        FunctionDefinition("multiply", fixed(AnySingular, AnySingular), Number, MultiplyFunction),
        FunctionDefinition("divide", fixed(AnySingular, AnySingular), Number, DivideFunction),
        FunctionDefinition("modulo", fixed(AnySingular, AnySingular), Number, ModuloFunction),
        FunctionDefinition("negate", fixed(AnySingular, AnySingular), Number, ModuloFunction),
    )
}
