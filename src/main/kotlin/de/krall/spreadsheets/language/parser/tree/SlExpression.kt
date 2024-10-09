package de.krall.spreadsheets.language.parser.tree

abstract class SlExpression : AbstractSlElement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitExpression(this, data)
    }
}
