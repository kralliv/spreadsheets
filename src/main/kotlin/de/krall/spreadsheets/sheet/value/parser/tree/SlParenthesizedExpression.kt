package de.krall.spreadsheets.sheet.value.parser.tree

import de.krall.spreadsheets.sheet.value.parser.SlSource

class SlParenthesizedExpression(
    val expression: SlExpression,
    override val source: SlSource? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitParenthesizedExpression(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        expression.accept(visitor, data)
    }
}
