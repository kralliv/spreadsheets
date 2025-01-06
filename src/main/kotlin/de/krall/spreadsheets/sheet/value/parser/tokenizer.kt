package de.krall.spreadsheets.sheet.value.parser

import java.util.EnumSet

interface TokenSequenceWalker {

    val token: Token

    fun advance(includeWhitespace: Boolean = false)

    fun eof(): Boolean

    fun at(type: TokenType): Boolean
    fun at(types: TokenTypeSet): Boolean

    fun lookahead(offset: Int, includeWhitespace: Boolean = false): Token?

    fun span(): Span

    interface Span {
        fun rollback()
        fun finish(): SlSource
    }
}

typealias TokenTypeSet = EnumSet<TokenType>

class SimpleTokenSequenceWalker(private val input: TokenSequence) : TokenSequenceWalker {

    private val tokens = input.toArray()

    private var index = -1

    private var currentToken: Token? = null
    override val token: Token
        get() {
            ensureInitiallyAdvanced()
            return currentToken ?: error("eof")
        }

    private fun ensureInitiallyAdvanced() {
        if (index == -1) {
            advance()
        }
    }

    override fun advance(includeWhitespace: Boolean) {
        val tokens = tokens
        var index = index

        if (index >= tokens.size) return

        while (index < tokens.size) {
            val token = tokens.getOrNull(++index) ?: break
            if (includeWhitespace || token.type != TokenType.WHITESPACE) break
        }

        move(index)
    }

    private fun move(index: Int) {
        this.index = index
        this.currentToken = tokens.getOrNull(index)
    }

    override fun eof(): Boolean {
        ensureInitiallyAdvanced()
        return index >= tokens.size
    }

    override fun at(type: TokenType): Boolean {
        ensureInitiallyAdvanced()
        val currentType = currentToken?.type ?: return false
        return currentType == type
    }

    override fun at(types: TokenTypeSet): Boolean {
        ensureInitiallyAdvanced()
        val currentType = currentToken?.type ?: return false
        return types.contains(currentType)
    }

    override fun lookahead(offset: Int, includeWhitespace: Boolean): Token? {
        val tokens = tokens
        var index = index

        repeat(offset) {
            while (index < tokens.size) {
                val token = tokens.getOrNull(++index) ?: break
                if (includeWhitespace || token.type != TokenType.WHITESPACE) break
            }
        }

        return tokens.getOrNull(index)
    }

    private fun spanStartIndex(): Int {
        return index
//        return if (index == -1) 0 else index
    }

    private fun spanEndIndex(): Int {
        return index
    }

    override fun span(): TokenSequenceWalker.Span {
        return SpanImpl(spanStartIndex())
    }

    private inner class SpanImpl(
        private val startIndex: Int,
    ) : TokenSequenceWalker.Span {

        override fun rollback() {
            move(startIndex)
        }

        override fun finish(): SlSource {
            val endIndex = spanEndIndex()

            val startPosition = position(startIndex)
            val endPosition = position(endIndex)

            val segment = input.input.segment(startPosition, endPosition - startPosition)

            return SlSource(segment)
        }

        private fun position(index: Int): Int {
            if (index < 0) return firstPosition()
            if (index >= tokens.size) return lastPosition()
            return tokens[index].offset
        }

        private fun firstPosition(): Int {
            if (tokens.isEmpty()) return 0
            val firstToken = tokens.first()
            return firstToken.offset
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
