package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.Location

class SlParenthesizedExpression(
    val expression: SlExpression,
    override val location: Location? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitParenthesizedExpression(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        expression.accept(visitor, data)
    }
}
