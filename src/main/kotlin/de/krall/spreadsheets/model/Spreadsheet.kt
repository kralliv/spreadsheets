package de.krall.spreadsheets.model

import de.krall.spreadsheets.value.EvaluatedValue
import de.krall.spreadsheets.value.Value

interface Spreadsheet {

    operator fun get(row: Int, column: Int): Cell

    fun addListener(listener: SpreadsheetListener)
    fun removeListener(listener: SpreadsheetListener)
}

interface Cell {
    val row: Int
    val column: Int

    var value: Value?

    val evaluatedValue: EvaluatedValue?
}

interface SpreadsheetListener {
    fun cellChanged(cell: Cell)
}
