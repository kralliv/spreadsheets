package de.krall.spreadsheets.value

import de.krall.spreadsheets.grid.Area
import de.krall.spreadsheets.grid.Cell

sealed interface Referencing

class Reference(val cell: Cell) : Referencing

class ReferenceRange(val area: Area) : Referencing
