package de.krall.spreadsheets.sheet.io

import de.krall.spreadsheets.sheet.Cell
import de.krall.spreadsheets.sheet.Row
import de.krall.spreadsheets.sheet.Spreadsheet
import de.krall.spreadsheets.sheet.value.Value
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private const val VERSION = 1
private val MAGIC_NUMBER = byteArrayOf(0x23, 0x7E, 0x1A, 0x0D)

private object ValueTypes {
    const val NULL = 0
    const val TEXT = 1
    const val NUMBER = 2
    const val FORMULA = 3
}

fun Spreadsheet.writeTo(outputStream: OutputStream) {
    val output = DataOutputStream(outputStream.buffered())

    output.writeByte(VERSION)
    output.write(MAGIC_NUMBER)

    val contentOutput = DataOutputStream(GZIPOutputStream(output, true))

    val rows = rows.toList()

    contentOutput.writeInt(rows.size)
    for (row in rows) {
        row.writeRow(contentOutput)
    }

    contentOutput.flush()
}

private fun Row.writeRow(output: DataOutput) {
    output.writeInt(row)

    val cells = cells.toList()

    output.writeInt(cells.size)
    for (cell in cells) {
        cell.writeCell(output)
    }
}

private fun Cell.writeCell(output: DataOutput) {
    output.writeInt(column)

    when (val value = value) {
        null -> output.writeByte(ValueTypes.NULL)
        is Value.Text -> {
            output.writeByte(ValueTypes.TEXT)
            output.writeUTF(value.text)
        }

        is Value.Number -> {
            output.writeByte(ValueTypes.NUMBER)
            output.writeDouble(value.number)
        }

        is Value.Formula -> {
            output.writeByte(ValueTypes.FORMULA)
            output.writeUTF(value.formula)
        }
    }
}

fun Spreadsheet.readFrom(inputStream: InputStream) {
    val input = DataInputStream(inputStream.buffered())

    val version = input.read()
    val magicNumber = input.readNBytes(4)
    if (!magicNumber.contentEquals(MAGIC_NUMBER)) throw IOException("invalid format")
    if (version != VERSION) throw IOException("unsupported version: $version")

    val contentInput = DataInputStream(GZIPInputStream(input))

    val rowCount = contentInput.readInt()
    for (rowIndex in 0..<rowCount) {
        readRow(contentInput)
    }
}

private fun Spreadsheet.readRow(input: DataInput) {
    val row = input.readInt()

    val cellCount = input.readInt()

    for (columnIndex in 0 until cellCount) {
        readCell(input, row)
    }
}

private fun Spreadsheet.readCell(input: DataInput, row: Int) {
    val column = input.readInt()

    val valueType = input.readByte().toInt()
    val value = when (valueType) {
        ValueTypes.NULL -> null
        ValueTypes.TEXT -> Value.Text(input.readUTF())
        ValueTypes.NUMBER -> Value.Number(input.readDouble())
        ValueTypes.FORMULA -> Value.Formula(input.readUTF())
        else -> throw IOException("unsupported value type: $valueType")
    }

    val cell = get(row, column)
    cell.value = value
}
