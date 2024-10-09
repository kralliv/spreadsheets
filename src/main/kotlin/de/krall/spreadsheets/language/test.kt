package de.krall.spreadsheets.language

import de.krall.spreadsheets.language.parser.Processor

fun main() {
    val text = "=foo(1)"

    val processor = Processor()

    val (statement, diagnostics) = processor.process(text)

    println(statement)
    diagnostics.forEach { println(it) }
}
