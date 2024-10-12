package de.krall.spreadsheets.value

sealed class Reference {
    data class Cell(val cell: de.krall.spreadsheets.grid.Cell) : Reference()
    data class Area(val area: de.krall.spreadsheets.grid.Area) : Reference()
}
