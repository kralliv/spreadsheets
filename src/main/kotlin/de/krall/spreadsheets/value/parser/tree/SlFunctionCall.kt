package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.parser.SlSource
import de.krall.spreadsheets.value.parser.type.FunctionDefinition

class SlFunctionCall(
    val name: String,
    val arguments: List<SlExpression>,
    override val source: SlSource? = null,
) : SlExpression() {

    var functionOrNull: FunctionDefinition? = null

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitFunctionCall(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        arguments.forEach { it.accept(visitor, data) }
    }
}
