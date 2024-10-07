package de.krall.spreadsheets.expression.parser

import de.krall.spreadsheets.expression.Formula
import de.krall.spreadsheets.expression.Value

class Parser(val tokenizer: ParserInput) {

    fun parse(tokens: TokenSequence, diagnostics: DiagnosticSink): Value {
        val input = ParserInput(tokens)

        return parseValue(input)
    }

    private fun parseValue(input: ParserInput): Value {
        if (input.at(TokenType.EQ)) {
            return Value.Formula(parseFormula(input))
        } else if (input.at(TokenType.NUMBER) && input.lookahead(1) == null) {
            return Value.Number(input.token!!.number)
        } else {
            val text = buildString {
                while (!input.eof()) {
                    append(input.token!!.text)
                }
            }
            return Value.String(text)
        }
    }

    private fun parseFormula(input: ParserInput): Formula {

    }
}
