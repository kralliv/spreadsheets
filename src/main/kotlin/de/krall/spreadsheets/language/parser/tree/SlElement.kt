package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.SlSource

interface SlElement {

    val source: SlSource?

    fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R

    fun accept(visitor: SlVisitorVoid) {
        accept(visitor, null)
    }

    fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D)

    fun acceptChildren(visitor: SlVisitorVoid) {
        acceptChildren(visitor, null)
    }
}
