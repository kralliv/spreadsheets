package de.krall.spreadsheets.sheet

import de.krall.spreadsheets.sheet.value.EvaluatedValue
import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.Value
import java.io.Closeable

interface Spreadsheet : Closeable {

    operator fun get(row: Int, column: Int): Cell

    val rows: Sequence<Row>

    fun addListener(listener: SpreadsheetListener)
    fun removeListener(listener: SpreadsheetListener)
}

interface Row {
    val row: Int

    operator fun get(column: Int): Cell

    val cells: Sequence<Cell>
}

interface Cell {
    val row: Int
    val column: Int

    var value: Value?

    val evaluatedValue: EvaluatedValue?
}

interface SpreadsheetListener {
    fun cellChanged(cell: Cell, previousCell: Cell)
    fun cellUpdated(cell: Cell)
}

val Cell.reference: Reference
    get() = Reference(de.krall.spreadsheets.sheet.grid.Cell(column, row))
