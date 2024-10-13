package de.krall.spreadsheets.model

import de.krall.spreadsheets.value.EvaluatedValue
import de.krall.spreadsheets.value.Value
import java.io.Closeable

interface Spreadsheet : Closeable {

    operator fun get(column: Int, row: Int): Cell

    fun addListener(listener: SpreadsheetListener)
    fun removeListener(listener: SpreadsheetListener)
}

interface Cell {
    val column: Int
    val row: Int

    var value: Value?

    val evaluatedValue: EvaluatedValue?
}

interface SpreadsheetListener {
    fun cellChanged(cell: Cell)
}
