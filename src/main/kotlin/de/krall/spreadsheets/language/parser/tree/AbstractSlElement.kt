package de.krall.spreadsheets.language.parser.tree

abstract class AbstractSlElement : SlElement {

    override fun toString(): String = buildString {
        append(this@AbstractSlElement::class.simpleName)
        source?.let { source ->
            append(" at position ")
            append(source.offset)
            append(" '")
            append(source.text)
            append("'")
        }
    }
}
