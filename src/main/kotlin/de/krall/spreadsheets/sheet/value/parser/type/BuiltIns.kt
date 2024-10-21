package de.krall.spreadsheets.sheet.value.parser.type

import de.krall.spreadsheets.sheet.value.formula.function.AddFunction
import de.krall.spreadsheets.sheet.value.formula.function.DelayFunction
import de.krall.spreadsheets.sheet.value.formula.function.DivideFunction
import de.krall.spreadsheets.sheet.value.formula.function.MinusFunction
import de.krall.spreadsheets.sheet.value.formula.function.ModuloFunction
import de.krall.spreadsheets.sheet.value.formula.function.MultiplyFunction
import de.krall.spreadsheets.sheet.value.formula.function.NegateFunction
import de.krall.spreadsheets.sheet.value.formula.function.PowFunction
import de.krall.spreadsheets.sheet.value.formula.function.SqrtFunction
import de.krall.spreadsheets.sheet.value.formula.function.SumFunction

object BuiltIns {

    val Any = AnyType
    val Text = TextType
    val Number = NumberType
    val Reference = ReferenceType
    val ReferenceRange = ReferenceRangeType
    val Nothing = NothingType
    val Error = ErrorType

    val AnySingular = UnionType(Text, Number, Reference)

    val functions = listOf(
        FunctionDefinition("sum", variadic(Any), Number, SumFunction),
        FunctionDefinition("add", fixed(AnySingular, AnySingular), Number, AddFunction),
        FunctionDefinition("minus", fixed(AnySingular, AnySingular), Number, MinusFunction),
        FunctionDefinition("multiply", fixed(AnySingular, AnySingular), Number, MultiplyFunction),
        FunctionDefinition("divide", fixed(AnySingular, AnySingular), Number, DivideFunction),
        FunctionDefinition("modulo", fixed(AnySingular, AnySingular), Number, ModuloFunction),
        FunctionDefinition("negate", fixed(AnySingular, AnySingular), Number, NegateFunction),
        FunctionDefinition("pow", fixed(AnySingular, AnySingular), Number, PowFunction),
        FunctionDefinition("sqrt", fixed(AnySingular), Number, SqrtFunction),

        FunctionDefinition("delay", fixed(Number), Number, DelayFunction),
    )
}
