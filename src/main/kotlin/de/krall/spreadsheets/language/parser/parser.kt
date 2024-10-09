package de.krall.spreadsheets.language.parser

import de.krall.spreadsheets.language.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.language.parser.tree.SlExpression
import de.krall.spreadsheets.language.parser.tree.SlFormulaStatement
import de.krall.spreadsheets.language.parser.tree.SlFunctionCall
import de.krall.spreadsheets.language.parser.tree.SlInvalid
import de.krall.spreadsheets.language.parser.tree.SlLiteral
import de.krall.spreadsheets.language.parser.tree.SlNumberStatement
import de.krall.spreadsheets.language.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.language.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.language.parser.tree.SlReference
import de.krall.spreadsheets.language.parser.tree.SlStatement
import de.krall.spreadsheets.language.parser.tree.SlTextStatement
import de.krall.spreadsheets.util.plus
import de.krall.spreadsheets.util.toEnumSet

private val ADDITIVE_OPERATORS = OperatorSet(
    TokenType.PLUS to SlBinaryExpression.Operator.PLUS,
    TokenType.MINUS to SlBinaryExpression.Operator.MINUS
)
private val MULTIPLICATIVE_OPERATORS = OperatorSet(
    TokenType.ASTERISK to SlBinaryExpression.Operator.TIMES,
    TokenType.SOLIDUS to SlBinaryExpression.Operator.DIVIDE,
    TokenType.PERCENT to SlBinaryExpression.Operator.MODULO,
)

private val PREFIX_OPERATORS = OperatorSet(
    TokenType.PLUS to SlPrefixExpression.Operator.PLUS,
    TokenType.MINUS to SlPrefixExpression.Operator.MINUS,
)

private val LITERAL_EXPRESSION_START = TokenTypeSet.of(TokenType.NUMBER)
private val PARENTHESIZED_EXPRESSION_START = TokenTypeSet.of(TokenType.LPAREN)
private val ATOMIC_EXPRESSION_START = TokenTypeSet.of(TokenType.IDENTIFIER) + PARENTHESIZED_EXPRESSION_START + LITERAL_EXPRESSION_START
private val PREFIX_EXPRESSION_START = PREFIX_OPERATORS.types + ATOMIC_EXPRESSION_START
private val EXPRESSION_START = PREFIX_EXPRESSION_START + ATOMIC_EXPRESSION_START

private val PARENTHESIZED_RECOVERY_SET = TokenTypeSet.of(TokenType.RPAREN)
private val FUNCTION_CALL_RECOVERY_SET = TokenTypeSet.of(TokenType.COMMA, TokenType.RPAREN)
private val ARGUMENT_RECOVERY_SET = TokenTypeSet.of(TokenType.COMMA)

class SlParser(input: Segment, tokens: TokenSequence, diagnostics: DiagnosticSink) : AbstractParser(input, tokens, diagnostics) {

    fun parse(): SlStatement {
        return when {
            at(TokenType.EQ) -> parseFormulaStatement()
            at(TokenType.NUMBER) && lookahead(1) == null -> parseNumberStatement()
            else -> parseTextStatement()
        }
    }

    private fun parseFormulaStatement(): SlFormulaStatement {
        assert(at(TokenType.EQ))

        val span = span()

        advance() // EQ

        val expression = if (recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)) {
            parseExpression()
        } else {
            SlInvalid()
        }

        return SlFormulaStatement(expression, span.finish())
    }

    private fun parseNumberStatement(): SlNumberStatement {
        assert(at(TokenType.NUMBER))

        val span = span()

        val number = token.number
        advance() // NUMBER

        return SlNumberStatement(number, span.finish())
    }

    private fun parseTextStatement(): SlTextStatement {
        val span = span()

        val text = buildString {
            while (!eof()) {
                append(token.text)
                advance()
            }
        }

        return SlTextStatement(text, span.finish())
    }

    private fun parseExpression(): SlExpression {
        return parseAdditiveExpression()
    }

    private fun parseAdditiveExpression(): SlExpression {
        return parseBinaryExpression(ADDITIVE_OPERATORS) { parseMultiplicativeExpression() }
    }

    private fun parseMultiplicativeExpression(): SlExpression {
        return parseBinaryExpression(MULTIPLICATIVE_OPERATORS) { parsePrefixExpression() }
    }

    private inline fun parseBinaryExpression(operators: OperatorSet<SlBinaryExpression.Operator>, higher: () -> SlExpression): SlExpression {
        assert(at(EXPRESSION_START))

        val span = span()

        var left = withRecoverySet(operators.types) {
            higher()
        }

        while (recover(operators.types, Diagnostics.UNEXPECTED_TOKEN)) {
            val operator = operators[token.type]
            advance()

            val right = withRecoverySet(operators.types) {
                if (recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)) {
                    higher()
                } else {
                    SlInvalid(span().finish())
                }
            }

            left = SlBinaryExpression(left, operator, right, span.finish())
        }

        return left
    }

    private fun parsePrefixExpression(): SlExpression {
        assert(at(PREFIX_EXPRESSION_START))

        return when {
            at(PREFIX_OPERATORS.types) -> {
                val span = span()

                val operator = PREFIX_OPERATORS[token.type]
                advance()

                val expression = parsePrefixExpression()

                SlPrefixExpression(operator, expression, span.finish())
            }

            else -> parseAtomicExpression()
        }
    }

    private fun parseAtomicExpression(): SlExpression {
        assert(at(ATOMIC_EXPRESSION_START))

        return when {
            at(TokenType.LPAREN) -> parseParenthesizedExpression()
            isAtFunctionCallExpression() -> parseFunctionCallExpression()
            at(TokenType.IDENTIFIER) -> parseReferenceExpression()
            at(LITERAL_EXPRESSION_START) -> parseLiteralExpression()
            else -> error("preconditions violated")
        }
    }

    private fun parseParenthesizedExpression(): SlExpression {
        assert(at(TokenType.LPAREN))

        val span = span()

        advance() // LPAREN

        val expression = withRecoverySet(PARENTHESIZED_RECOVERY_SET) {
            if (recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)) {
                parseExpression()
            } else {
                SlInvalid(span.finish())
            }
        }

        if (at(TokenType.RPAREN)) {
            advance() // RPAREN
        } else {
            report(Diagnostics.EXPECTED_CLOSING_PARENTHESIS.at(segment(span().finish())))
        }

        return SlParenthesizedExpression(expression, span.finish())
    }

    private fun isAtFunctionCallExpression(): Boolean {
        return at(TokenType.IDENTIFIER) && lookahead(1)?.type == TokenType.LPAREN
    }

    private fun parseFunctionCallExpression(): SlExpression {
        assert(isAtFunctionCallExpression())

        val span = span()

        val name = token.text
        advance() // IDENTIFIER

        advance() // LPAREN

        val arguments = withRecoverySet(FUNCTION_CALL_RECOVERY_SET) {
            parseArgumentList()
        }

        if (at(TokenType.RPAREN)) {
            advance() // RPAREN
        } else {
            report(Diagnostics.EXPECTED_CLOSING_PARENTHESIS.at(segment(span().finish())))
        }

        return SlFunctionCall(name, arguments, span.finish())
    }

    private fun parseArgumentList(): List<SlExpression> {
        val arguments = mutableListOf<SlExpression>()
        while (!eof() && !at(TokenType.RPAREN)) {
            val hasExpression = recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)

            if (hasExpression) {
                val expression = parseExpression()

                arguments.add(expression)
            }

            if (at(TokenType.COMMA)) {
                if (!hasExpression) {
                    val expression = SlInvalid(span().finish())

                    arguments.add(expression)
                }

                advance() // COMMA
            }
        }
        return arguments
    }

    private fun parseReferenceExpression(): SlExpression {
        assert(at(TokenType.IDENTIFIER))

        val span = span()

        val name = token.text
        advance() // IDENTIFIER

        return SlReference(name, span.finish())
    }

    private fun parseLiteralExpression(): SlExpression {
        assert(at(LITERAL_EXPRESSION_START))

        return when {
            at(TokenType.NUMBER) -> {
                val span = span()

                val number = token.number
                advance() // NUMBER

                SlLiteral(number, span.finish())
            }

            else -> error("precondition violated")
        }
    }
}

private class OperatorSet<T>(
    vararg mappings: Pair<TokenType, T>,
) {
    private val mapping = mappings.toMap()

    val types: TokenTypeSet = mapping.keys.toEnumSet()

    operator fun get(type: TokenType): T {
        return mapping[type] ?: error("token type $type is unknown to operator set")
    }
}
