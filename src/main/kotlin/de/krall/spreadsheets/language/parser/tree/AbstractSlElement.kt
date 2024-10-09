package de.krall.spreadsheets.language.parser.tree

abstract class AbstractSlElement : SlElement {

    override fun toString(): String = "${this::class.simpleName} at $location"
}
