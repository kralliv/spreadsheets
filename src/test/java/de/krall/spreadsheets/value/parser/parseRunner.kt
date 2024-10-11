package de.krall.spreadsheets.value.parser

fun main() {
    init()

    repeat(100000) {
        test()
    }
}

private fun init() {
    val text = "=(121+ 1)*sum(a1, 5)"

    val processor = ValueParser()

    val (statement, diagnostics) = processor.process(text)

    println(statement)
    diagnostics.forEach { println(it) }
}

private fun test() {
    val text = "=(121+ 1)*sum(a1, 5)"

    val processor = ValueParser()

    val (statement, diagnostics) = processor.process(text)
}
