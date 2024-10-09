package de.krall.spreadsheets.language.parser

import de.krall.spreadsheets.language.parser.check.FunctionCallChecker
import de.krall.spreadsheets.language.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.language.parser.tree.SlStatement

class LanguageProcessor {

    private val checkers = listOf(
        FunctionCallChecker,
    )

    fun process(value: String): ProcessingResult {
        val context = ProcessingContext()

        var statement = parse(value, context)

        analyse(statement, context)

        if (context.hasErrors()) {
            return ProcessingResult(statement = null, context.diagnostics)
        }

        statement = transform(statement, context)

        if (context.hasErrors()) {
            return ProcessingResult(statement = null, context.diagnostics)
        }

        return ProcessingResult(statement, context.diagnostics)
    }

    private fun parse(value: String, context: ProcessingContext): SlStatement {
        val input = Segment(value)
        val lexer = SlLexer(input)
        val parser = SlParser(lexer, context)

        return parser.parse()
    }

    private fun analyse(statement: SlStatement, context: ProcessingContext) {
        for (checker in checkers) {
            checker.check(statement, context)
        }
    }

    private fun transform(statement: SlStatement, context: ProcessingContext): SlStatement {
        return statement
    }
}

data class ProcessingResult(
    val statement: SlStatement?,
    val diagnostics: List<Diagnostic>,
)
