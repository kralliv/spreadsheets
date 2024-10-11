package de.krall.spreadsheets.value.parser

import de.krall.spreadsheets.value.TreeDumper
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostic
import de.krall.spreadsheets.value.parser.tree.SlValue
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
        val resources = TestCaseResource.resolve("spreadsheets/test/language/parser", "txt")

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

    private fun parse(text: String): Pair<SlValue, List<Diagnostic>> {
        val context = ProcessingContext()

        val input = Segment(text)
        val lexer = SlLexer(input)
        val parser = SlParser(lexer, context)

        val statement = parser.parseValue()

        return statement to context.diagnostics
    }

    private fun dump(statement: SlValue, diagnostics: List<Diagnostic>): String {
        val buffer = StringBuilder()
        statement.accept(TreeDumper(buffer), 0)
        buffer.appendLine()
        for (diagnostic in diagnostics) {
            buffer.append(diagnostic.severity)
                .append(" ")
                .append(diagnostic.name)

            diagnostic.source?.let { source ->
                buffer.append(" (")
                    .append(source.offset)
                    .append("-")
                    .append(source.offset + source.length)
                    .append(")")
            }

            buffer.appendLine()
        }
        return buffer.toString()
    }
}
