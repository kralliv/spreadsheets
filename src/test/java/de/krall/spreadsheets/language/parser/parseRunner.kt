package de.krall.spreadsheets.language.parser

fun main() {
    val text = ".0"

    val processor = LanguageProcessor()

    val (statement, diagnostics) = processor.process(text)

    println(statement)
    diagnostics.forEach { println(it) }
}
