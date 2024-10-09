package de.krall.spreadsheets.language.parser.tree

import de.krall.spreadsheets.language.parser.Location

interface SlElement {

    val location: Location?

    fun <D, R> accept(visitor: SlVisitor<D, R>, data: D): R

    fun <D, R> acceptChildren(visitor: SlVisitor<D, R>, data: D)
}
