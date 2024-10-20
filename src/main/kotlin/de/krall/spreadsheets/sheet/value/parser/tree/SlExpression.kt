package de.krall.spreadsheets.sheet.value.parser.tree

import de.krall.spreadsheets.sheet.value.parser.type.Type

abstract class SlExpression : AbstractSlElement() {

    var typeOrNull: Type? = null

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitExpression(this, data)
    }
}
