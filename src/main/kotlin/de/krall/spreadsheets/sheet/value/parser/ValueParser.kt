package de.krall.spreadsheets.sheet.value.parser

import de.krall.spreadsheets.sheet.value.ParsedValue
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.formula.Formula
import de.krall.spreadsheets.sheet.value.parser.analysis.ReferenceResolver
import de.krall.spreadsheets.sheet.value.parser.analysis.TypeResolver
import de.krall.spreadsheets.sheet.value.parser.builder.FormulaBuilder
import de.krall.spreadsheets.sheet.value.parser.builder.ParsedValueBuilder
import de.krall.spreadsheets.sheet.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.sheet.value.parser.tree.SlExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlStatement

class ValueParser {

    fun parseValue(value: String): Value {
        val context = ProcessingContext()

        val input = Segment(value)
        val lexer = SlLexer(input)
        val parser = SimpleParser(lexer, context)

        return parser.parseValue()
    }

    fun parseParsedValue(value: String): ParsedValue {
        val (statement, _) = parseParsedValueTree(value)

        return when (statement) {
            null -> ParsedValue.BadFormula
            else -> ParsedValueBuilder.build(statement)
        }
    }

    fun parseParsedValueTree(value: String): ParseResult<SlStatement> {
        return process(value) { it.parseValue() }
    }

    fun parseFormula(value: String): Formula? {
        val (expression, _) = parseFormulaTree(value)

        return expression?.let { FormulaBuilder.build(it) }
    }

    fun parseFormulaTree(value: String): ParseResult<SlExpression> {
        return process(value) { it.parseFormula() }
    }

    private fun <T : SlElement> process(value: String, parse: (SlParser) -> T): ParseResult<T> {
        val context = ProcessingContext()

        var element = parse(value, context, parse)

        analyse(element, context)

        if (context.hasErrors()) {
            return ParseResult(result = null, context.diagnostics)
        }

        element = transform(element, context)

        if (context.hasErrors()) {
            return ParseResult(result = null, context.diagnostics)
        }

        return ParseResult(element, context.diagnostics)
    }

    private fun <T : SlElement> parse(value: String, context: ProcessingContext, parse: (SlParser) -> T): T {
        val input = Segment(value)
        val lexer = SlLexer(input)
        val parser = SlParser(lexer, context)

        return parse(parser)
    }

    private val analysers = listOf(
        ReferenceResolver,
        TypeResolver,
    )

    private fun analyse(element: SlElement, context: ProcessingContext) {
        for (analyser in analysers) {
            analyser.check(element, context)
        }
    }

    private fun <T : SlElement> transform(element: T, context: ProcessingContext): T {
        return element
    }
}

data class ParseResult<T>(
    val result: T?,
    val diagnostics: List<Diagnostic>,
)
