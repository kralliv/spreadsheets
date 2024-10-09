package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

class SlBinaryExpression(
    val left: SlExpression,
    val operator: Operator,
    val right: SlExpression,
    override val source: SlSource? = null,
) : SlExpression() {

    enum class Operator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDE,
        MODULO,
    }

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitBinaryExpression(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        left.accept(visitor, data)
        right.accept(visitor, data)
    }
}
