package de.krall.spreadsheets.sheet.value.parser

interface TokenSequence {

    val input: Segment

    fun nextToken(): Token?
}

class SlLexer(override val input: Segment) : TokenSequence {

    private val reader = Reader(input.chars, input.offset, input.length)

    override fun nextToken(): Token? {
        if (reader.isEof()) return null

        val start = reader.bufferPosition
        val kind = nextTokenKind()
        val end = reader.bufferPosition

        val length = end - start
        check(length > 0) { "lexer produced a token type, but did not consume any chars at position $start" }

        val segment = input.segment(start, length)

        return when (kind) {
            is TokenKind.Normal -> Token(kind.type, segment)
            is TokenKind.String -> StringToken(kind.type, segment, kind.string)
            is TokenKind.Numeric -> NumericToken(kind.type, segment, kind.number)
        }
    }

    private fun nextTokenKind(): TokenKind {
        return when (reader.c) {
            ' ', '\t', '\n', '\r' -> {
                reader.nextChar()

                while (!reader.isEof()) {
                    when (reader.c) {
                        ' ', '\t', '\n', '\r' -> reader.nextChar()
                        else -> break
                    }
                }

                TokenKind.Normal(TokenType.WHITESPACE)
            }

            in 'a'..'z', in 'A'..'Z' -> readIdentifierLike()

            in '0'..'9' -> readNumberLike()
            '.' -> {
                if (reader.peekChar() in '0'..'9') {
                    readNumberLike()
                } else {
                    reader.nextChar()
                    TokenKind.Normal(TokenType.DELIMITER)
                }
            }

            '"' -> readString()

            '=' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.EQ)
            }

            '+' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.PLUS)
            }

            '-' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.MINUS)
            }

            '*' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.ASTERISK)
            }

            '/' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.SOLIDUS)
            }

            '%' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.PERCENT)
            }

            ':' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.COLON)
            }

            ',' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.COMMA)
            }

            '(' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.LPAREN)
            }

            ')' -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.RPAREN)
            }

            else -> {
                reader.nextChar()
                TokenKind.Normal(TokenType.DELIMITER)
            }
        }
    }

    private fun readIdentifierLike(): TokenKind {
        check(reader.c in 'a'..'z' || reader.c in 'A'..'Z')

        reader.putChar()

        while (true) {
            when (reader.c) {
                in 'a'..'z', in 'A'..'Z' -> reader.putChar()
                in '0'..'9' -> reader.putChar()
                else -> break
            }
        }

        return when (val identifier = reader.chars()) {
            "true" -> TokenKind.Normal(TokenType.TRUE)
            "false" -> TokenKind.Normal(TokenType.FALSE)
            else -> TokenKind.String(TokenType.IDENTIFIER, identifier)
        }
    }

    private fun readNumberLike(): TokenKind {
        check(reader.c in '0'..'9' || reader.c == '.')

        while (reader.c in '0'..'9') {
            reader.putChar()
        }

        if (reader.c == '.') reader.putChar()

        while (reader.c in '0'..'9') {
            reader.putChar()
        }

        val text = reader.chars()
        var numeric = text
        if (numeric.startsWith('.')) numeric = "0$text"
        if (numeric.endsWith('.')) numeric = "${text}0"

        val number = numeric.toDoubleOrNull()
        if (number != null) {
            return TokenKind.Numeric(TokenType.NUMBER, number)
        }

        return TokenKind.Normal(TokenType.TEXT)
    }

    private fun readString(): TokenKind {
        check(reader.c == '"')

        reader.nextChar() // "

        while (reader.hasChar()) {
            when (reader.c) {
                '"' -> break
                '\\' -> {
                    reader.nextChar()
                    if (reader.hasChar()) {
                        reader.putChar()
                    }
                }

                else -> reader.putChar()
            }
        }

        reader.nextChar() // "

        return TokenKind.String(TokenType.STRING, reader.chars())
    }

    private sealed class TokenKind {
        abstract val type: TokenType

        data class Normal(override val type: TokenType) : TokenKind()
        data class String(override val type: TokenType, val string: kotlin.String) : TokenKind()
        data class Numeric(override val type: TokenType, val number: Double) : TokenKind()
    }
}

