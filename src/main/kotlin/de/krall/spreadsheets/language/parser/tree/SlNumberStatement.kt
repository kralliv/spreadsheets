package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.Location
import java.math.BigDecimal

class SlNumberStatement(
    val number: BigDecimal,
    override val location: Location? = null,
) : SlStatement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitNumberStatement(this, data)
    }

    override fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D) {}
}
