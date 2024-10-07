package de.krall.spreadsheets.expression.parser

class ParserInput(tokens: TokenSequence) {

    private val tokens = tokens.toArray()

    fun next(): TokenType? {

    }

    val token: Token?

    fun eof(): Boolean {

    }

    fun at(kind: TokenType): Boolean {

    }

    fun lookahead(ahead: Int): Token? {

    }
}

private fun TokenSequence.toArray(): Array<Token> {
    val tokens = mutableListOf<Token>()
    while (true) {
        val token = nextToken() ?: break
        tokens.add(token)
    }
    return tokens.toTypedArray()
}
