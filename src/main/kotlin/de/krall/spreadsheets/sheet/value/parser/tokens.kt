package de.krall.spreadsheets.sheet.value.parser

open class Token(
    val type: TokenType,
    val segment: Segment,
) {

    val text: String
        get() = segment.text

    val offset: Int
        get() = segment.offset
    val length: Int
        get() = segment.length

    open val string: String
        get() = error("token $type has no string")

    open val number: Double
        get() = error("token $type has no number")

    override fun toString(): String = "$type '$text'"
}

class StringToken(
    type: TokenType,
    segment: Segment,
    override val string: String,
) : Token(type, segment)

class NumericToken(
    type: TokenType,
    segment: Segment,
    override val number: Double,
) : Token(type, segment)

enum class TokenType {
    WHITESPACE,
    IDENTIFIER,
    NUMBER,
    STRING,
    TEXT,
    TRUE,
    FALSE,
    EQ,
    PLUS,
    MINUS,
    ASTERISK,
    SOLIDUS,
    PERCENT,
    COLON,
    COMMA,
    LPAREN,
    RPAREN,
    DELIMITER,
}
