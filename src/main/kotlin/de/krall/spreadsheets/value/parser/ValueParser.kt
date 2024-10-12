package de.krall.spreadsheets.value.parser

import de.krall.spreadsheets.value.parser.analysis.ReferenceChecker
import de.krall.spreadsheets.value.parser.analysis.TypeResolver
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlExpression
import de.krall.spreadsheets.value.parser.tree.SlValue

class ValueParser {

    private val checkers = listOf(
        TypeResolver,
        ReferenceChecker,
    )

    fun parseValue(text: String): ParseResult<SlValue> {
        return process(text) { it.parseValue() }
    }

    fun parseFormula(text: String): ParseResult<SlExpression> {
        return process(text) { it.parseFormula() }
    }

    private fun <T : SlElement> process(value: String, parse: (SlParser) -> T): ParseResult<T> {
        val context = ProcessingContext()

        var element = parse(value, context, parse)

        analyse(element, context)

        if (context.hasErrors()) {
            return ParseResult(statement = null, context.diagnostics)
        }

        element = transform(element, context)

        if (context.hasErrors()) {
            return ParseResult(statement = null, context.diagnostics)
        }

        return ParseResult(element, context.diagnostics)
    }

    private fun <T : SlElement> parse(value: String, context: ProcessingContext, parse: (SlParser) -> T): T {
        val input = Segment(value)
        val lexer = SlLexer(input)
        val parser = SlParser(lexer, context)

        return parse(parser)
    }

    private fun analyse(element: SlElement, context: ProcessingContext) {
        for (checker in checkers) {
            checker.check(element, context)
        }
    }

    private fun <T : SlElement> transform(element: T, context: ProcessingContext): T {
        return element
    }
}

data class ParseResult<T : SlElement>(
    val statement: T?,
    val diagnostics: List<Diagnostic>,
)
