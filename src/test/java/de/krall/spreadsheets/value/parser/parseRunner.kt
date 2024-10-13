package de.krall.spreadsheets.value.parser

import de.krall.spreadsheets.value.TreeDumper

fun main() {
    val text = "a a"

    val processor = ValueParser()

    val (statement, diagnostics) = processor.parseParsedValueTree(text)

    println(statement)
    diagnostics.forEach { println(it) }
    println(buildString {
        statement?.accept(TreeDumper(this), 0)
    })
}
