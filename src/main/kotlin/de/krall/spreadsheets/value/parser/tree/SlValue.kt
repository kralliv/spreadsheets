package de.krall.spreadsheets.value.parser.tree

abstract class SlValue : AbstractSlElement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitStatement(this, data)
    }
}
