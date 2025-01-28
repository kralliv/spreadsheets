package de.krall.spreadsheets.sheet.value

import de.krall.spreadsheets.sheet.grid.Area
import de.krall.spreadsheets.sheet.grid.Cell
import de.krall.spreadsheets.sheet.grid.Column
import de.krall.spreadsheets.sheet.grid.Rectangle
import de.krall.spreadsheets.sheet.grid.Row

sealed interface Referencing

data class Reference(val cell: Cell) : Referencing {

    val row: Int
        get() = cell.y

    val column: Int
        get() = cell.x

    override fun toString(): String {
        val column = cell.x.toColumnName()
        val row = (cell.y + 1).toString()

        return "$column$row"
    }

    companion object
}

data class ReferenceRange(val area: Area) : Referencing {

    override fun toString(): String {
        return when (area) {
            is Rectangle -> {
                val startColumn = area.x.toColumnName()
                val startRow = (area.y + 1).toString()

                val endColumn = (area.x + area.width - 1).toColumnName()
                val endRow = (area.y + area.height).toString()

                return "$startColumn$startRow:$endColumn$endRow"
            }

            is Column -> {
                val startColumn = area.x.toColumnName()
                val startRow = when {
                    area.y != null -> (area.y + 1).toString()
                    else -> ""
                }

                val endColumn = (area.x + area.width - 1).toColumnName()

                return "$startColumn$startRow:$endColumn"
            }

            is Row -> {
                val startColumn = when {
                    area.x != null -> area.x.toColumnName()
                    else -> ""
                }
                val startRow = (area.y + 1).toString()

                val endRow = (area.y + area.height).toString()

                return "$startColumn$startRow:$endRow"
            }

            else -> "?:?"
        }
    }

    companion object
}

private fun Int.toColumnName(): String {
    require(this >= 0) { "column indices cannot be negative" }

    // Int.MAX_VALUE results in 7 characters
    val buffer = CharArray(7)
    var position = buffer.lastIndex

    var remainder = this
    while (remainder >= 26) {
        buffer[position--] = ('A' + (remainder % 26))

        remainder = remainder / 26 - 1
    }
    buffer[position] = ('A' + remainder)


    return String(buffer, position, (buffer.size - position))
}
