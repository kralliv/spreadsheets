package de.krall.spreadsheets.sheet.value.parser.tree

abstract class SlStatement : AbstractSlElement() {

    override fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R {
        return visitor.visitStatement(this, data)
    }
}
