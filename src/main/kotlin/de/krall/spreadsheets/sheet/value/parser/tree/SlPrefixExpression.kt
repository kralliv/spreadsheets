package de.krall.spreadsheets.sheet.value.parser.tree

import de.krall.spreadsheets.sheet.value.parser.SlSource

class SlPrefixExpression(
    val operator: Operator,
    val expression: SlExpression,
    override val source: SlSource? = null,
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
