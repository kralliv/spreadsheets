package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.parser.SlSource

class SlTextValue(
    val text: String,
    override val source: SlSource? = null,
) : SlValue() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitTextStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
