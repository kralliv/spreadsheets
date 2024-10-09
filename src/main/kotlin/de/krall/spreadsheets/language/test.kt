package de.krall.spreadsheets.language

import de.krall.spreadsheets.language.parser.SlLexer
import de.krall.spreadsheets.language.parser.SlParser
import de.krall.spreadsheets.language.parser.Segment

fun main() {
    val text = "foo"
    val input = Segment(text)
    val lexer = SlLexer(input)
    val parser = SlParser(input, lexer) { println(it) }

    val statement = parser.parse()

    println(statement)
}
