package de.krall.spreadsheets.sheet.value.parser

class SlSource(private val segment: Segment) {

    val offset: Int
        get() = segment.offset

    val length: Int
        get() = segment.length

    val text: String
        get() = segment.text

    override fun toString(): String = "position $offset '$text'"
}
