package de.krall.spreadsheets.expression.parser

import java.util.LinkedList
import de.krall.spreadsheets.util.plus

abstract class AbstractParser(
    val input: Segment,
    tokens: TokenSequence,
    private val diagnostics: DiagnosticSink,
) : TokenSequenceWalker {

    private val delegate = SimpleTokenSequenceWalker(tokens)

    override val token: Token
        get() = delegate.token

    override fun advance() = delegate.advance()

    override fun eof(): Boolean = delegate.eof()

    override fun at(type: TokenType): Boolean = delegate.at(type)
    override fun at(types: TokenTypeSet): Boolean = delegate.at(types)

    override fun lookahead(offset: Int): Token? = delegate.lookahead(offset)

    override fun span(): TokenSequenceWalker.Span = delegate.span()

    fun report(diagnostic: Diagnostic) {
        diagnostics.report(diagnostic)
    }

    fun segment(location: Location): Segment {
        return input.segment(location.offset, location.length)
    }

    private val recoverySets = LinkedList<TokenTypeSet>()

    fun pushRecoverySet(recoverySet: TokenTypeSet) {
        val current = recoverySets.peek()
        val new = if (current != null) recoverySet + current else recoverySet
        recoverySets.push(new)
    }

    fun popRecoverySet() {
        recoverySets.pop()
    }

    fun recover(set: TokenTypeSet, diagnosticFactory: DiagnosticFactory0): Boolean {
        var span = span()
        while (!eof()) {
            if (at(set)) return true
            if (recoverySets.isNotEmpty() && at(recoverySets.first())) return false

            advance()
            report(diagnosticFactory.at(segment(span.finish())))
            span = span()
        }
        return false
    }

    inline fun <T> withRecoverySet(set: TokenTypeSet, block: () -> T): T {
        pushRecoverySet(set)
        val result = block()
        popRecoverySet()
        return result
    }
}