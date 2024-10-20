package de.krall.spreadsheets.sheet.value.parser

import de.krall.spreadsheets.sheet.value.Value

class SimpleParser(tokens: TokenSequence, context: ProcessingContext) : AbstractParser(tokens, context) {

    fun parseValue(): Value {
        return when {
            at(TokenType.EQ) -> parseFormulaValue()
            at(TokenType.NUMBER) && lookahead(1) == null -> parseNumberValue()
            else -> parseTextValue()
        }
    }

    private fun parseFormulaValue(): Value.Formula {
        assert(at(TokenType.EQ))

        advance() // EQ

        val span = span()

        while (!eof()) {
            advance()
        }

        val source = span.finish()

        return Value.Formula(source.text)
    }

    private fun parseNumberValue(): Value.Number {
        assert(at(TokenType.NUMBER) && lookahead(1) == null)

        val number = token.number
        advance() // NUMBER

        return Value.Number(number)
    }

    private fun parseTextValue(): Value.Text {
        val span = span()

        while (!eof()) {
            advance()
        }

        val source = span.finish()

        return Value.Text(source.text)
    }
}