package de.krall.spreadsheets.sheet.value.parser

fun Segment(text: String): Segment {
    return Segment(text.toCharArray(), 0, text.length)
}

class Segment(val chars: CharArray, val offset: Int, override val length: Int) : CharSequence {

    val text: String
        get() = String(chars, offset, length)

    fun segment(offset: Int, length: Int): Segment {
        checkRange(offset, length, this.length)
        return Segment(chars, this.offset + offset, length)
    }

    override fun get(index: Int): Char {
        checkIndex(index, length)
        return chars[offset + index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        checkRange(startIndex, endIndex - startIndex, length)
        return Segment(chars, offset + startIndex, offset + endIndex - startIndex)
    }

    override fun toString(): String = text
}

private fun checkIndex(index: Int, available: Int) {
    if (index < 0 || index >= available) throw IndexOutOfBoundsException("index: $index, available: $available")
}

private fun checkRange(offset: Int, length: Int, available: Int) {
    if (offset < 0 || length < 0 || offset + length > available) throw IndexOutOfBoundsException("offset: $offset, length: $length, available: $available")
}