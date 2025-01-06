package de.krall.spreadsheets.sheet.value.lexer

import de.krall.spreadsheets.sheet.value.parser.*
import de.krall.spreadsheets.test.TestCaseResource
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals

class LexTest {

    @TestFactory
    fun createTests(): Iterable<DynamicNode> {
        val resources = TestCaseResource.resolve("spreadsheets/test/value/lexer", "txt")

        return resources.map { createTestCase(it) }
    }

    private fun createTestCase(resource: TestCaseResource): DynamicNode {
        return DynamicTest.dynamicTest(resource.name, resource.outputFile.toUri()) {
            val input = resource.inputFile.readText()
            val actualOutput = lexAndDump(input)
            if (resource.outputFile.exists()) {
                val expectedOutput = resource.outputFile.readText()

                assertEquals(expectedOutput.trim(), actualOutput.trim())
            } else {
                resource.outputFile.createFile().writeText(actualOutput)
            }
        }
    }

    private fun lexAndDump(text: String): String {
        val tokens = lex(text)
        return dump(tokens)
    }

    private fun lex(input: String): TokenSequence {
        return SlLexer(Segment(input))
    }

    private fun dump(tokens: TokenSequence): String {
        val buffer = StringBuilder()
        for (token in tokens.asSequence()) {
            buffer.append(token.type)
                .append(" ")
                .append(token.offset)
                .append("-")
                .append(token.offset + token.length)
                .append(" '")
                .append(token.text)
                .append("'")

            when (token) {
                is StringToken -> buffer.append(" '").append(token.string).append("'")
                is NumericToken -> buffer.append(" ").append(token.number.toString())
            }

            buffer.appendLine()
        }
        return buffer.toString()
    }

    private fun TokenSequence.asSequence(): Sequence<Token> {
        return sequence {
            while (true) {
                val token = nextToken() ?: break
                yield(token)
            }
        }
    }
}