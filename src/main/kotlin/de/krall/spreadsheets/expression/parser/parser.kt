package de.krall.spreadsheets.expression.parser

import de.krall.spreadsheets.util.plus
import de.krall.spreadsheets.util.toEnumSet

private val ADDITIVE_OPERATORS = OperatorSet(
    TokenType.PLUS to BinaryExpression.Operator.PLUS,
    TokenType.MINUS to BinaryExpression.Operator.MINUS
)
private val MULTIPLICATIVE_OPERATORS = OperatorSet(
    TokenType.ASTERISK to BinaryExpression.Operator.TIMES,
    TokenType.SOLIDUS to BinaryExpression.Operator.DIVIDE,
    TokenType.PERCENT to BinaryExpression.Operator.MODULO,
)

private val PREFIX_OPERATORS = OperatorSet(
    TokenType.PLUS to PrefixExpression.Operator.PLUS,
    TokenType.MINUS to PrefixExpression.Operator.MINUS,
)

private val LITERAL_EXPRESSION_START = TokenTypeSet.of(TokenType.NUMBER)
private val PARENTHESIZED_EXPRESSION_START = TokenTypeSet.of(TokenType.LPAREN)
private val ATOMIC_EXPRESSION_START = TokenTypeSet.of(TokenType.IDENTIFIER) + PARENTHESIZED_EXPRESSION_START + LITERAL_EXPRESSION_START
private val PREFIX_EXPRESSION_START = PREFIX_OPERATORS.types + ATOMIC_EXPRESSION_START
private val EXPRESSION_START = PREFIX_EXPRESSION_START + ATOMIC_EXPRESSION_START

private val PARENTHESIZED_RECOVERY_SET = TokenTypeSet.of(TokenType.RPAREN)
private val FUNCTION_CALL_RECOVERY_SET = TokenTypeSet.of(TokenType.COMMA, TokenType.RPAREN)
private val ARGUMENT_RECOVERY_SET = TokenTypeSet.of(TokenType.COMMA)

class Parser(input: Segment, tokens: TokenSequence, diagnostics: DiagnosticSink) : AbstractParser(input, tokens, diagnostics) {

    fun parse(): Statement {
        return when {
            at(TokenType.EQ) -> parseFormulaStatement()
            at(TokenType.NUMBER) && lookahead(1) == null -> parseNumberStatement()
            else -> parseTextStatement()
        }
    }

    private fun parseFormulaStatement(): FormulaStatement {
        assert(at(TokenType.EQ))

        val span = span()

        advance() // EQ

        val expression = if (recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)) {
            parseExpression()
        } else {
            InvalidExpression()
        }

        return FormulaStatement(expression, span.finish())
    }

    private fun parseNumberStatement(): NumberStatement {
        assert(at(TokenType.NUMBER))

        val span = span()

        val number = token.number
        advance() // NUMBER

        return NumberStatement(number, span.finish())
    }

    private fun parseTextStatement(): TextStatement {
        val span = span()

        val text = buildString {
            while (!eof()) {
                append(token.text)
            }
        }

        return TextStatement(text, span.finish())
    }

    private fun parseExpression(): Expression {
        return parseAdditiveExpression()
    }

    private fun parseAdditiveExpression(): Expression {
        return parseBinaryExpression(ADDITIVE_OPERATORS) { parseMultiplicativeExpression() }
    }

    private fun parseMultiplicativeExpression(): Expression {
        return parseBinaryExpression(MULTIPLICATIVE_OPERATORS) { parsePrefixExpression() }
    }

    private inline fun parseBinaryExpression(operators: OperatorSet<BinaryExpression.Operator>, higher: () -> Expression): Expression {
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
                    InvalidExpression(span().finish())
                }
            }

            left = BinaryExpression(left, operator, right, span.finish())
        }

        return left
    }

    private fun parsePrefixExpression(): Expression {
        assert(at(PREFIX_EXPRESSION_START))

        return when {
            at(PREFIX_OPERATORS.types) -> {
                val span = span()

                val operator = PREFIX_OPERATORS[token.type]
                advance()

                val expression = parsePrefixExpression()

                PrefixExpression(operator, expression, span.finish())
            }

            else -> parseAtomicExpression()
        }
    }

    private fun parseAtomicExpression(): Expression {
        assert(at(ATOMIC_EXPRESSION_START))

        return when {
            at(TokenType.LPAREN) -> parseParenthesizedExpression()
            isAtFunctionCallExpression() -> parseFunctionCallExpression()
            at(TokenType.IDENTIFIER) -> parseReferenceExpression()
            at(LITERAL_EXPRESSION_START) -> parseLiteralExpression()
            else -> error("preconditions violated")
        }
    }

    private fun parseParenthesizedExpression(): Expression {
        assert(at(TokenType.RPAREN))

        val span = span()

        advance() // LPAREN

        val expression = withRecoverySet(PARENTHESIZED_RECOVERY_SET) {
            if (recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)) {
                parseExpression()
            } else {
                InvalidExpression(span.finish())
            }
        }

        if (at(TokenType.LPAREN)) {
            advance() // RPAREN
        } else {
            report(Diagnostics.EXPECTED_CLOSING_PARENTHESIS.at(segment(span().finish())))
        }

        return ParenthesizedExpression(expression, span.finish())
    }

    private fun isAtFunctionCallExpression(): Boolean {
        return at(TokenType.IDENTIFIER) && lookahead(1)?.type == TokenType.LPAREN
    }

    private fun parseFunctionCallExpression(): Expression {
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

        return FunctionCallExpression(name, arguments, span.finish())
    }

    private fun parseArgumentList(): List<Expression> {
        val arguments = mutableListOf<Expression>()
        while (!eof() && !at(TokenType.RPAREN)) {
            val hasExpression = recover(EXPRESSION_START, Diagnostics.EXPECTED_EXPRESSION)

            if (hasExpression) {
                val expression = parseExpression()

                arguments.add(expression)
            }

            if (at(TokenType.COMMA)) {
                if (!hasExpression) {
                    val expression = InvalidExpression(span().finish())

                    arguments.add(expression)
                }

                advance() // COMMA
            }
        }
        return arguments
    }

    private fun parseReferenceExpression(): Expression {
        assert(at(TokenType.IDENTIFIER))

        val span = span()

        val name = token.text
        advance() // IDENTIFIER

        return ReferenceExpression(name, span.finish())
    }

    private fun parseLiteralExpression(): Expression {
        assert(at(LITERAL_EXPRESSION_START))

        return when {
            at(TokenType.NUMBER) -> {
                val span = span()

                val number = token.number
                advance() // NUMBER

                LiteralExpression(number, span.finish())
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
