package de.krall.spreadsheets.ui

class SpreadsheetModel {

    private val values = mutableMapOf<Position, String>()

    fun getValue(row: Int, column: Int): String {
        return values[Position(row, column)] ?: ""
    }

    fun setValue(row: Int, column: Int, value: String) {
        if (value.isNotEmpty()) {
            values[Position(row, column)] = value
        } else {
            values.remove(Position(row, column))
        }
    }

    private data class Position(val row: Int, val column: Int)
}