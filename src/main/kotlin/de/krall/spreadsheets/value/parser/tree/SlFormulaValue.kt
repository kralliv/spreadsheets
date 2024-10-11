package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.parser.SlSource

class SlFormulaValue(
    val expression: SlExpression,
    override val source: SlSource? = null,
) : SlValue() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitFormulaStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        expression.accept(visitor, data)
    }
}
