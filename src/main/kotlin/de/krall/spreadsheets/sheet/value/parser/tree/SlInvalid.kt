package de.krall.spreadsheets.sheet.value.parser.tree

import de.krall.spreadsheets.sheet.value.parser.SlSource

class SlInvalid(
    override val source: SlSource? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitInvalid(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
