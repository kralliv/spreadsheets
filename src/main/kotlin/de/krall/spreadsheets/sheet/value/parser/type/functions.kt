package de.krall.spreadsheets.sheet.value.parser.type

import de.krall.spreadsheets.sheet.value.formula.function.Function

internal fun fixed(vararg type: Type): ParameterTypes {
    return ParameterTypes(type.toList(), variadic = null)
}

internal fun variadic(type: Type): ParameterTypes {
    return ParameterTypes(fixed = listOf(), variadic = type)
}

class FunctionDefinition(
    val name: String,
    val parameterTypes: ParameterTypes,
    val returnType: Type,
    val function: Function,
) {

    override fun toString(): String = "$name($parameterTypes): $returnType"
}

data class ParameterTypes(
    val fixed: List<Type>,
    val variadic: Type?,
) {

    override fun toString(): String = buildString {
        for ((index, type) in fixed.withIndex()) {
            if (index > 0) {
                append(", ")
            }
            append(type)
        }
        if (variadic != null) {
            if (fixed.isNotEmpty()) {
                append(", ")
            }
            append(variadic)
            append("...")
        }
    }
}
