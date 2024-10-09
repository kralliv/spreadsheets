package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

class SlReference(
    val name: String,
    override val source: SlSource? = null,
) : SlExpression() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitReference(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
