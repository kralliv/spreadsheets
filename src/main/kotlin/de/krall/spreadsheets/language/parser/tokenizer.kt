package de.krall.spreadsheets.language.parser

import java.util.EnumSet

interface TokenSequenceWalker {

    val token: Token

    fun advance()

    fun eof(): Boolean

    fun at(type: TokenType): Boolean
    fun at(types: TokenTypeSet): Boolean

    fun lookahead(offset: Int): Token?

    fun span(): Span

    interface Span {
        fun finish(): SlSource
    }
}

typealias TokenTypeSet = EnumSet<TokenType>

class SimpleTokenSequenceWalker(private val input: TokenSequence) : TokenSequenceWalker {

    private val tokens = input.toArray()

    private var index = -1

    private var currentToken: Token? = null
    override val token: Token
        get() = currentToken ?: error("eof")

    init {
        advance()
    }

    override fun advance() {
        val tokens = tokens
        var index = index

        if (index >= tokens.size) return

        while (index < tokens.size) {
            val token = tokens.getOrNull(++index) ?: break
            if (token.type != TokenType.WHITESPACE) break
        }

        this.index = index
        this.currentToken = tokens.getOrNull(index)
    }

    override fun eof(): Boolean = index >= tokens.size

    override fun at(type: TokenType): Boolean {
        val currentType = currentToken?.type ?: return false
        return currentType == type
    }

    override fun at(types: TokenTypeSet): Boolean {
        val currentType = currentToken?.type ?: return false
        return types.contains(currentType)
    }

    override fun lookahead(offset: Int): Token? {
        val tokens = tokens
        var index = index

        repeat(offset) {
            while (index < tokens.size) {
                val token = tokens.getOrNull(++index) ?: break
                if (token.type != TokenType.WHITESPACE) break
            }
        }

        return tokens.getOrNull(index)
    }

    override fun span(): TokenSequenceWalker.Span = SpanImpl(index)

    private inner class SpanImpl(
        private val startIndex: Int,
    ) : TokenSequenceWalker.Span {

        override fun finish(): SlSource {
            val endIndex = index

            val startPosition = tokens.getOrNull(startIndex)?.offset ?: lastPosition()
            val endPosition = tokens.getOrNull(endIndex)?.offset ?: lastPosition()

            val segment = input.input.segment(startPosition, endPosition - startPosition)

            return SlSource(segment)
        }

        private fun lastPosition(): Int {
            if (tokens.isEmpty()) return 0
            val lastToken = tokens.last()
            return lastToken.offset + lastToken.length
        }
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
