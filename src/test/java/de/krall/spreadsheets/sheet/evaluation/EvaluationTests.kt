package de.krall.spreadsheets.sheet.evaluation

import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.sheet.SpreadsheetImpl
import de.krall.spreadsheets.sheet.reference
import de.krall.spreadsheets.sheet.value.EvaluatedValue
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.Value
import de.krall.spreadsheets.sheet.value.parser.Reader
import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.sheet.value.parser.parseOrNull
import de.krall.spreadsheets.test.TestCaseResource
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals

// Input:
//   Text             T
//   Numeric          N
//   Formula          F
//
// Output:
//   Blank            B
//   Unevaluated      U
//   Text             T
//   Numeric          N
//   Reference        R
//   Reference Range  A
//   Error            E

class EvaluationTests {

    @TestFactory
    fun createTests(): Iterable<DynamicNode> {
        val resources = TestCaseResource.resolve("spreadsheets/test/sheet/evaluation", "txt")

        return resources.map { createTestCase(it) }
    }

    private fun createTestCase(resource: TestCaseResource): DynamicNode {
        return DynamicTest.dynamicTest(resource.name, resource.outputFile.toUri()) {
            val input = TestCaseInputs(resource.inputFile.readText())
            val actualOutput = evaluateAndDump(input)
            if (resource.outputFile.exists()) {
                val expectedOutput = resource.outputFile.readText()

                assertEquals(expectedOutput.trim(), actualOutput.trim())
            } else {
                resource.outputFile.createFile().writeText(actualOutput)
            }
        }
    }

    private fun evaluateAndDump(inputs: TestCaseInputs): String {
        return withSpreadsheet { spreadsheet ->
            evaluate(spreadsheet, inputs)

            dump(spreadsheet)
        }
    }

    private inline fun <R> withSpreadsheet(block: (Spreadsheet) -> R): R {
        val spreadsheet = SpreadsheetImpl(ValueParser())
        return try {
            block(spreadsheet)
        } finally {
            spreadsheet.close()
        }
    }

    private fun evaluate(spreadsheet: Spreadsheet, inputs: TestCaseInputs) {
        // Populate the cells, order must not matter for the final result
        for (cell in inputs.cells) {
            spreadsheet[cell.row, cell.column].value = cell.value
        }

        // Force the evaluation in case of lazy-evaluation
        for (cell in inputs.cells) {
            spreadsheet[cell.row, cell.column].evaluatedValue
        }

        Thread.sleep(250)
    }

    private fun dump(spreadsheet: Spreadsheet): String {
        val buffer = StringBuilder()

        spreadsheet.rows.forEach { row ->
            row.cells.forEach { cell ->
                val value = cell.evaluatedValue

                buffer.append(cell.reference)
                    .append(" ")

                when (value) {
                    null -> buffer.append("B")
                    EvaluatedValue.Unevaluated -> buffer.append("U")
                    is EvaluatedValue.Text -> buffer.append("T'").append(value.text).append("'")
                    is EvaluatedValue.Number -> buffer.append("N'").append(value.number).append("'")
                    is EvaluatedValue.Error -> buffer.append("E'").append(value.error).append("'")
                }

                buffer.appendLine()
            }
        }

        return buffer.toString()
    }
}

private data class TestCaseInputs(
    val cells: List<CellInput>,
)

private data class CellInput(
    val cell: Reference,
    val value: Value?,
) {
    val row: Int
        get() = cell.row

    val column: Int
        get() = cell.column
}

private fun TestCaseInputs(input: String): TestCaseInputs {
    val cells = input.lines()
        .filter { it.isNotBlank() }
        .map { parseCellInput(it) }

    return TestCaseInputs(cells)
}

private fun parseCellInput(line: String): CellInput {
    val reader = Reader(line)

    val reference = readReference(reader)

    reader.skipWhitespaceAndComment()

    val value = readValue(reader)

    reader.skipWhitespaceAndComment()

    check(reader.isEof()) { "expected eof" }

    return CellInput(reference, value)
}

private fun readReference(reader: Reader): Reference {
    check(reader.hasChar()) { "expected reference" }

    while (reader.hasChar() && !reader.c.isWhitespace()) {
        reader.putChar()
    }

    val reference = reader.chars()
    return Reference.parseOrNull(reference) ?: error("bad reference: $reference")
}

private fun readValue(reader: Reader): Value? {
    check(reader.hasChar()) { "expected value" }

    reader.putChar()

    val type = reader.chars()

    val literal = when (reader.c) {
        '\'' -> readValueLiteral(reader)
        else -> null
    }

    if (type == "B") {
        check(literal == null) { "blank value should have a literal" }
        return null
    }

    check(literal != null) { "expected literal for value of type $type" }

    return when (type) {
        "T" -> Value.Text(literal)
        "N" -> Value.Number(literal.toDouble())
        "F" -> Value.Formula(literal)
        else -> error("unknown value type $type")
    }
}

private fun readValueLiteral(reader: Reader): String {
    check(reader.c == '\'') { "expected start of literal" }

    reader.nextChar() // '

    while (reader.hasChar()) {
        when (reader.c) {
            '\\' -> {
                reader.nextChar()
                if (reader.hasChar()) {
                    reader.putChar()
                }
            }

            '\'' -> break
            else -> reader.putChar()
        }
    }

    reader.nextChar() // '

    return reader.chars()
}

private fun Reader.skipWhitespaceAndComment() {
    while (c.isWhitespace()) {
        nextChar()
    }
    if (c == '#') {
        while (hasChar()) {
            nextChar()
        }
    }
}
