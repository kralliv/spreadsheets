package de.krall.spreadsheets.value.parser.analysis

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.parser.tree.SlExpression
import de.krall.spreadsheets.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.type.FunctionDefinition
import de.krall.spreadsheets.value.parser.type.Type

val SlExpression.type: Type
    get() = typeOrNull ?: error("type has not been resolved yet: $this")

val SlReference.reference: Reference
    get() = referenceOrNull ?: error("reference has not been resolved yet: $this")

val SlFunctionCall.function: FunctionDefinition
    get() = functionOrNull ?: error("function has not been resolved yet: $this")
