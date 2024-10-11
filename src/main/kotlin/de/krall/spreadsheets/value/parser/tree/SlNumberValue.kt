package de.krall.spreadsheets.value.parser.tree

import de.krall.spreadsheets.value.parser.SlSource
import java.math.BigDecimal

class SlNumberValue(
    val number: BigDecimal,
    override val source: SlSource? = null,
) : SlValue() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitNumberStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
