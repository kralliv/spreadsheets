package de.krall.spreadsheets.language.parser

private const val CHAR_EOF = '\u0000'

class Reader(
    private val buffer: CharArray,
    private val offset: Int,
    private val length: Int,
) {

    constructor(text: String) : this(text.toCharArray(), 0, text.length)

    var bufferPosition: Int = offset - 1
        private set

    @JvmField
    var c: Char = ' '

    var line: Int = 0
        private set

    var column: Int = -1
        private set

    private var chars = CharArray(128)
    var charsLength: Int = 0
        private set

    init {
        checkRange(offset, length, buffer.size)

        nextChar()
    }

    fun nextChar() {
        if (bufferPosition >= length) return

        if (c == '\n') {
            line++
            column = 0
        }

        bufferPosition++
        column++

        c = when {
            bufferPosition < length -> buffer[bufferPosition]
            else -> CHAR_EOF
        }

        if (c == '\r') {
            if (peekChar() == '\n') {
                bufferPosition++
                column++
            }
            c = '\n'
        } else if (c == '\u000C') {
            c = '\n'
        }
    }

    fun hasChar(): Boolean = bufferPosition < length

    fun isEof(): Boolean = bufferPosition >= length

    fun peekChar(): Char {
        return peekChar(1)
    }

    fun peekChar(nth: Int): Char {
        val bufferPosition = bufferPosition
        if (bufferPosition + nth >= length) return CHAR_EOF
        return buffer[bufferPosition + nth]
    }

    fun putChar(c: Char = this.c, read: Boolean = true) {
        ensureCapacity(charsLength)

        chars[charsLength++] = c

        if (read) {
            nextChar()
        }
    }

    fun hasChars(): Boolean = charsLength > 0

    fun chars(): String {
        val text = String(chars, 0, charsLength)
        charsLength = 0
        return text
    }

    private fun ensureCapacity(length: Int) {
        if (length >= buffer.size) {
            chars = growBuffer(buffer, length)
        }
    }

    private fun growBuffer(buffer: CharArray, length: Int): CharArray {
        var newLength = buffer.size

        while (newLength < length + 1) {
            newLength *= 2
        }

        val newBuffer = CharArray(newLength)
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
        return newBuffer
    }

    private fun checkRange(offset: Int, length: Int, available: Int) {
        if (offset < 0 || length < 0 || offset + length > available) throw IndexOutOfBoundsException("offset: $offset, length: $length, available: $available")
    }
}

fun Reader.nextChar(count: Int) {
    for (i in 0 until count) {
        nextChar()
    }
}

fun Reader.putChar(nth: Int) {
    for (i in 0 until nth) {
        putChar()
    }
}
