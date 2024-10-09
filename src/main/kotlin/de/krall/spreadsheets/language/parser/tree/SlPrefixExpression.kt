package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.Location

class SlPrefixExpression(
    val operator: Operator,
    val expression: SlExpression,
    override val location: Location? = null,
) : SlExpression() {

    enum class Operator {
        PLUS,
        MINUS,
    }

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitPrefixExpression(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        expression.accept(visitor, data)
    }
}
