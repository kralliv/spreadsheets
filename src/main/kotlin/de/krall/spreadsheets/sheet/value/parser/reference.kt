package de.krall.spreadsheets.sheet.value.parser

import de.krall.spreadsheets.sheet.grid.Cell
import de.krall.spreadsheets.sheet.grid.Column
import de.krall.spreadsheets.sheet.grid.Rectangle
import de.krall.spreadsheets.sheet.grid.Row
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange

fun Reference.Companion.parseOrNull(name: String): Reference? {
    val location = parseCellLocation(name) ?: return null

    // only single cell are allowed to be referenced
    if (location.isInfiniteRow || location.isInfiniteColumn) {
        return null
    }

    return Reference(Cell(location.column, location.row))
}

fun ReferenceRange.Companion.parseOrNull(startName: String, endName: String): ReferenceRange? {
    val startLocation = parseCellLocation(startName) ?: return null
    val endLocation = parseCellLocation(endName) ?: return null

    // double infinite ranges like 'B:4' are not allowed
    if (startLocation.isInfiniteRow && endLocation.isInfiniteColumn
        || startLocation.isInfiniteColumn && endLocation.isInfiniteRow
    ) {
        return null
    }

    val (minColumn, maxColumn) = when {
        startLocation.column < endLocation.column -> startLocation.column to endLocation.column
        else -> endLocation.column to startLocation.column
    }

    val (minRow, maxRow) = when {
        startLocation.row < endLocation.row -> startLocation.row to endLocation.row
        else -> endLocation.row to startLocation.row
    }

    val area = if (startLocation.isInfiniteRow || endLocation.isInfiniteRow) {
        val columnOffset = when {
            !startLocation.isInfiniteRow -> startLocation.row
            !endLocation.isInfiniteRow -> endLocation.row
            else -> null
        }

        Column(minColumn, maxColumn - minColumn + 1, columnOffset)
    } else if (startLocation.isInfiniteColumn || endLocation.isInfiniteColumn) {
        val rowOffset = when {
            !startLocation.isInfiniteColumn -> startLocation.column
            !endLocation.isInfiniteColumn -> endLocation.column
            else -> null
        }

        Row(minRow, maxRow - minRow + 1, rowOffset)
    } else {
        Rectangle(minColumn, minRow, maxColumn - minColumn + 1, maxRow - minRow + 1)
    }

    return ReferenceRange(area)
}

private data class CellLocation(val column: Int, val row: Int) {

    val isInfiniteRow: Boolean
        get() = row == -1

    val isInfiniteColumn: Boolean
        get() = column == -1
}

private fun parseCellLocation(text: String): CellLocation? {
    if (text.isEmpty()) return null

    val reader = Reader(text)

    var column = -1
    while (isLetter(reader.c)) {
        val value = reader.c.uppercaseChar() - 'A' + 1
        if (column != -1) {
            column *= 26
            column += value
        } else {
            column = value
        }

        reader.nextChar()
    }
    if (column != -1) {
        column -= 1
    }

    var row = -1
    while (isDigit(reader.c)) {
        val value = reader.c - '0'
        if (row != -1) {
            row *= 10
            row += value
        } else {
            row = value
        }

        reader.nextChar()
    }

    // Rows are one based in the UI
    if (row == 0) return null
    if (row > 0) {
        row -= 1
    }

    if (!reader.isEof()) return null

    return CellLocation(column, row)
}

private fun isLetter(c: Char): Boolean {
    return c in 'a'..'z' || c in 'A'..'Z'
}

private fun isDigit(c: Char): Boolean {
    return c in '0'..'9'
}
