package de.krall.spreadsheets.expression.parser

import java.math.BigDecimal

interface TokenSequence {
    fun nextToken(): Token?
}

class Lexer(val reader: Reader) {

    fun tokenize(input: String): TokenSequence {
        return LazyTokenSequence(input)
    }

    private inner class LazyTokenSequence(input: String) : TokenSequence {

        private val reader = Reader(input)

        override fun nextToken(): Token? {
            if (reader.isEof()) return null

            val start = reader.bufferPosition
            val kind = nextTokenKind()
            val end = reader.bufferPosition

            val length = end - start
            check(length > 0) { "lexer produced a token type, but did not consume any chars at position $start" }

            val segment = reader.segment(start, length)

            return when (kind) {
                is TokenKind.Normal -> Token(kind.type, segment)
                is TokenKind.String -> Token(kind.type, segment, string = kind.string)
                is TokenKind.Numeric -> Token(kind.type, segment, number = kind.number)
            }
        }
    }

    private fun nextTokenKind(): TokenKind {
        return when (reader.c) {
            in 'a'..'z', in 'A'..'Z' -> readIdentifier()

            in '0'..'9' -> readNumber()
            '.' -> {
                if (reader.peekChar() in '0'..'9') {
                    readNumber()
                } else {
                    TokenKind.Normal(TokenType.DELIMITER)
                }
            }

            '=' -> TokenKind.Normal(TokenType.EQ)
            '+' -> TokenKind.Normal(TokenType.PLUS)
            '-' -> TokenKind.Normal(TokenType.MINUS)
            '*' -> TokenKind.Normal(TokenType.ASTERISK)
            '/' -> TokenKind.Normal(TokenType.SOLIDUS)
            '%' -> TokenKind.Normal(TokenType.PERCENT)
            ',' -> TokenKind.Normal(TokenType.COMMA)
            '(' -> TokenKind.Normal(TokenType.LPAREN)
            ')' -> TokenKind.Normal(TokenType.RPAREN)

            else -> TokenKind.Normal(TokenType.DELIMITER)
        }
    }

    private fun readIdentifier(): TokenKind {
        check(reader.c in 'a'..'z' || reader.c in 'A'..'Z')

        reader.putChar()

        while (true) {
            when (reader.c) {
                in 'a'..'z', in 'A'..'Z' -> reader.putChar()
                in '0'..'9' -> reader.putChar()
                else -> break
            }
        }

        return TokenKind.String(TokenType.IDENTIFIER, reader.chars())
    }

    private fun readNumber(): TokenKind {
        check(reader.c in '0'..'9' || reader.c == '.')

        while (reader.c in '0'..'9') {
            reader.putChar()
        }

        if (reader.c == '.') reader.putChar()

        while (reader.c in '0'..'9') {
            reader.putChar()
        }

        var text = reader.chars()
        if (text.startsWith('.')) text = "0$text"
        if (text.endsWith('.')) text = "${text}0"

        return TokenKind.Numeric(TokenType.NUMBER, BigDecimal(text))
    }

    private sealed class TokenKind {
        abstract val type: TokenType

        data class Normal(override val type: TokenType) : TokenKind()
        data class String(override val type: TokenType, val string: kotlin.String) : TokenKind()
        data class Numeric(override val type: TokenType, val number: BigDecimal) : TokenKind()
    }
}

