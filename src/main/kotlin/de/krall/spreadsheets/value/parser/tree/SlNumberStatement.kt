package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.parser.SlSource

class SlNumberStatement(
    val number: Double,
    override val source: SlSource? = null,
) : SlStatement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitNumberStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
