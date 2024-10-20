package de.krall.spreadsheets.sheet.value.parser

class Token(
    val type: TokenType,
    val segment: Segment,
    string: String? = null,
    number: Double? = null,
) {

    val text: String
        get() = segment.text

    val offset: Int
        get() = segment.offset
    val length: Int
        get() = segment.length

    private val _string = string
    val string: String
        get() = _string ?: error("token $type has no string")

    private val _number = number
    val number: Double
        get() = _number ?: error("token $type has no number")

    override fun toString(): String = "$type '$text'"
}

enum class TokenType {
    WHITESPACE,
    IDENTIFIER,
    NUMBER,
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
