package de.krall.spreadsheets.language.parser

import de.krall.spreadsheets.language.parser.tree.SlStatement
import de.krall.spreadsheets.language.TreeDumper
import de.krall.spreadsheets.test.TestCaseResource
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals

class ParseTest {

    @TestFactory
    fun createTests(): Iterable<DynamicNode> {
        val resources = TestCaseResource.resolve("tests/language/parser", "txt")

        return resources.map { createTestCase(it) }
    }

    private fun createTestCase(resource: TestCaseResource): DynamicNode {
        return DynamicTest.dynamicTest(resource.name, resource.outputFile.toUri()) {
            val input = resource.inputFile.readText()
            val actualOutput = parseAndDump(input)
            if (resource.outputFile.exists()) {
                val expectedOutput = resource.outputFile.readText()

                assertEquals(expectedOutput.trim(), actualOutput.trim())
            } else {
                resource.outputFile.createFile().writeText(actualOutput)
            }
        }
    }

    private fun parseAndDump(text: String): String {
        val (statement, diagnostics) = parse(text)
        return dump(statement, diagnostics)
    }

    private fun parse(text: String): Pair<SlStatement, List<Diagnostic>> {
        val input = Segment(text)
        val lexer = SlLexer(input)
        val diagnosticCollector = DiagnosticCollector()
        val parser = SlParser(input, lexer, diagnosticCollector)

        val statement = parser.parse()
        val diagnostics = diagnosticCollector.toList()

        return statement to diagnostics
    }

    private fun dump(statement: SlStatement, diagnostics: List<Diagnostic>): String {
        val buffer = StringBuilder()
        statement.accept(TreeDumper(buffer), 0)
        buffer.appendLine()
        for (diagnostic in diagnostics) {
            buffer.append(diagnostic.severity)
                .append(" ")
                .append(diagnostic.message)
                .append(" (")
                .append(diagnostic.segment.offset)
                .append("-")
                .append(diagnostic.segment.offset + diagnostic.segment.length)
                .append(")")
                .appendLine()
        }
        return buffer.toString()
    }

}
