package de.krall.spreadsheets.util

import de.krall.spreadsheets.sheet.value.parser.Reader

fun String.parseCsv(): List<List<String>> {
    val lines = mutableListOf<List<String>>()

    val reader = Reader(this)
    while (!reader.isEof()) {
        lines.add(readLine(reader))

        if (reader.c == '\n') {
            reader.nextChar()
        }
    }

    return lines
}

private fun readLine(reader: Reader): List<String> {
    val values = mutableListOf<String>()

    var pending = true
    while (!reader.isEof() && reader.c != '\n') {
        pending = false

        while (!reader.isEof() && reader.c.isWhitespace()) {
            reader.nextChar()
        }

        val value = when (reader.c) {
            '"' -> readEscapedValue(reader)
            else -> readUnescapedValue(reader)
        }

        values.add(value)

        if (reader.c == ',') {
            pending = true
            reader.nextChar()
        }
    }

    if (pending) {
        values.add("")
    }

    return values
}

private fun readUnescapedValue(reader: Reader): String {
    while (!reader.isEof()) {
        when (reader.c) {
            ',', '\n' -> break
            else -> reader.putChar()
        }
    }
    return reader.chars().trim()
}

private fun readEscapedValue(reader: Reader): String {
    assert(reader.c == '"')

    reader.nextChar()

    while (!reader.isEof()) {
        when (reader.c) {
            '"' -> {
                if (reader.peekChar() != '"') break

                reader.putChar()
            }

            else -> reader.putChar()
        }
    }

    reader.nextChar()

    // skip misplaced characters outside of escape
    while (!reader.isEof()) {
        when (reader.c) {
            ',', '\n' -> break
            else -> reader.nextChar()
        }
    }

    return reader.chars()
}
