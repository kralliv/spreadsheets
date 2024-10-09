package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.Location

class SlInvalid(
    override val location: Location? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitInvalid(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
