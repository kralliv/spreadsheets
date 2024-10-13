package de.krall.spreadsheets.value.parser

import de.krall.spreadsheets.value.Value

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

        advance()

        val formula = buildString {
            while (!eof()) {
                append(token.text)
                advance()
            }
        }

        return Value.Formula(formula)
    }

    private fun parseNumberValue(): Value.Number {
        assert(at(TokenType.NUMBER) && lookahead(1) == null)

        val number = token.number
        advance() // NUMBER

        return Value.Number(number)
    }

    private fun parseTextValue(): Value.Text {
        val text = buildString {
            while (!eof()) {
                append(token.text)
                advance()
            }
        }

        return Value.Text(text)
    }
}