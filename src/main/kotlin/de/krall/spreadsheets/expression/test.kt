package de.krall.spreadsheets.expression

import de.krall.spreadsheets.expression.parser.Lexer
import de.krall.spreadsheets.expression.parser.Parser
import de.krall.spreadsheets.expression.parser.Segment

fun main() {
    val text = "=1:+1+:sum(,1,2,3,)"
    val input = Segment(text)
    val lexer = Lexer(input)
    val parser = Parser(input, lexer) { println(it) }

    val statement = parser.parse()

    println(statement)
}
