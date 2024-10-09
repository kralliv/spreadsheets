package de.krall.spreadsheets.language.parser

import de.krall.spreadsheets.language.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.language.parser.diagnotic.DiagnosticFactory0
import de.krall.spreadsheets.language.parser.tree.SlElement
import de.krall.spreadsheets.language.parser.tree.SlInvalid
import java.util.LinkedList
import de.krall.spreadsheets.util.plus

abstract class AbstractParser(
    tokens: TokenSequence,
    val context: ProcessingContext,
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
        context.report(diagnostic)
    }

    fun invalid(source: SlSource): SlElement {
        return SlInvalid(source)
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
            report(diagnosticFactory.on(invalid(span.finish())))
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