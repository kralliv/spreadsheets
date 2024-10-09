package de.krall.spreadsheets.language.parser.check

import de.krall.spreadsheets.language.parser.ProcessingContext
import de.krall.spreadsheets.language.parser.diagnotic.Diagnostics
import de.krall.spreadsheets.language.parser.tree.SlElement
import de.krall.spreadsheets.language.parser.tree.SlFunctionCall
import de.krall.spreadsheets.language.parser.tree.SlStatement
import de.krall.spreadsheets.language.parser.tree.SlVisitorVoid

object FunctionCallChecker : TreeChecker {

    private val SUPPORTED_FUNCTION_NAMES = listOf(
        "sum", "sub", "mul", "div", "mod",
    )

    override fun check(statement: SlStatement, context: ProcessingContext) {
        statement.accept(object : SlVisitorVoid() {
            override fun visitElement(element: SlElement) {
                element.acceptChildren(this)
            }

            override fun visitFunctionCall(functionCall: SlFunctionCall) {
                if (functionCall.name.lowercase() !in SUPPORTED_FUNCTION_NAMES) {
                    context.report(Diagnostics.UNKNOWN_FUNCTION.on(functionCall, functionCall.name))
                }
            }
        })
    }
}
