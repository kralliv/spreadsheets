package de.krall.spreadsheets.sheet.value

import de.krall.spreadsheets.grid.Area
import de.krall.spreadsheets.grid.Cell

sealed interface Referencing

data class Reference(val cell: Cell) : Referencing

data class ReferenceRange(val area: Area) : Referencing
