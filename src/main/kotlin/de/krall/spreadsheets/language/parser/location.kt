package de.krall.spreadsheets.language.parser

class Location(val offset: Int, val length: Int) {

    override fun toString(): String = buildString {
        append(offset)
        if (this@Location.length > 0) {
            append("-")
            append(offset + this@Location.length)
        }
    }
}
