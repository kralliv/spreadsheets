package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

class SlFunctionCall(
    val name: String,
    val arguments: List<SlExpression>,
    override val source: SlSource? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitFunctionCall(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        arguments.forEach { it.accept(visitor, data) }
    }
}
