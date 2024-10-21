package de.krall.spreadsheets.sheet.value.parser

import de.krall.spreadsheets.sheet.value.ParsedValue
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.formula.Formula
import de.krall.spreadsheets.sheet.value.parser.analysis.ReferenceResolver
import de.krall.spreadsheets.sheet.value.parser.analysis.TypeResolver
import de.krall.spreadsheets.sheet.value.parser.builder.FormulaBuilder
import de.krall.spreadsheets.sheet.value.parser.builder.ParsedValueBuilder
import de.krall.spreadsheets.sheet.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.sheet.value.parser.diagnotic.Severity
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.sheet.value.parser.tree.SlExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlStatement

class ValueParser {

    fun parseValue(value: String): Value {
        val context = ProcessingContext()

        val input = Segment(value)
        val lexer = SlLexer(input)
        val parser = SlParser(lexer, context)

        return parser.parseValue()
    }

    fun parseParsedValue(value: String): ParsedValue {
        val (statement, diagnostics) = parseParsedValueTree(value)

        return when {
            diagnostics.hasAtLeastErrors() -> ParsedValue.BadFormula
            else -> ParsedValueBuilder.build(statement)
        }
    }

    fun parseParsedValueTree(value: String): ParseResult<SlStatement> {
        return process(value) { it.parseStatement() }
    }

    fun parseFormula(value: String): Formula? {
        val (expression, diagnostics) = parseFormulaTree(value)

        if (diagnostics.hasAtLeastErrors()) return null

        return expression.let { FormulaBuilder.build(it) }
    }

    fun parseFormulaTree(value: String): ParseResult<SlExpression> {
        return process(value) { it.parseFormula() }
    }

    private fun <T : SlElement> process(value: String, parse: (SlParser) -> T): ParseResult<T> {
        val context = ProcessingContext()

        var element = parse(value, context, parse)

        analyse(element, context)

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

    private fun List<Diagnostic>.hasAtLeastErrors(): Boolean {
        return any { it.severity >= Severity.ERROR }
    }
}

data class ParseResult<T : SlElement>(
    val result: T,
    val diagnostics: List<Diagnostic>,
)
