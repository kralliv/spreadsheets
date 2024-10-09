package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

class SlFormulaStatement(
    val expression: SlExpression,
    override val source: SlSource? = null,
) : SlStatement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitFormulaStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {
        expression.accept(visitor, data)
    }
}
