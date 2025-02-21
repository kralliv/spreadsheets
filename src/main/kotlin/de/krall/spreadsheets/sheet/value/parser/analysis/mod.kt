package de.krall.spreadsheets.sheet.value.parser.analysis

import de.krall.spreadsheets.sheet.value.Referencing
import de.krall.spreadsheets.sheet.value.parser.tree.SlExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.sheet.value.parser.tree.SlReference
import de.krall.spreadsheets.sheet.value.parser.type.FunctionDefinition
import de.krall.spreadsheets.sheet.value.parser.type.Type

val SlExpression.type: Type
    get() = typeOrNull ?: error("type has not been resolved yet: $this")

val SlReference.referencing: Referencing
    get() = referencingOrNull ?: error("reference has not been resolved yet: $this")

val SlFunctionCall.function: FunctionDefinition
    get() = functionOrNull ?: error("function has not been resolved yet: $this")
