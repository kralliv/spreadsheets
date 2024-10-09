package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

class SlTextStatement(
    val text: String,
    override val source: SlSource? = null,
) : SlStatement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitTextStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
