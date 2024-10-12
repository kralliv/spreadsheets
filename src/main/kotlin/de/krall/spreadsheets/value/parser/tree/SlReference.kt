package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.parser.SlSource

class SlReference(
    val leftName: String,
    val rightName: String?,
    override val source: SlSource? = null,
) : SlExpression() {

    var referenceOrNull: Reference? = null

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitReference(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
