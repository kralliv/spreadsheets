package de.krall.spreadsheets.value

import de.krall.spreadsheets.grid.Cell
import de.krall.spreadsheets.value.parser.Reader
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlVisitorVoid

fun SlElement.references(): List<Reference> {
    val referenceElements = mutableListOf<SlReference>()
    accept(object : SlVisitorVoid() {
        override fun visitElement(element: SlElement) {
            element.acceptChildren(this)
        }

        override fun visitReference(reference: SlReference) {

        }
    })
    TODO()
}

private fun parseReference(reference: SlReference): Reference {
    val leftCellLocation = parseCellLocation(reference.leftName)

    if (reference.rightName == null) {
        if (leftCellLocation.row != -1 && leftCellLocation.column != -1) {
            return Reference.Cell(Cell(leftCellLocation.row, leftCellLocation.column))
        }


        TODO()
    }

    val rightCellLocation = parseCellLocation(reference.rightName)

    val (minRow, maxRow) = when {
        leftCellLocation.row == -1 -> rightCellLocation.row to leftCellLocation.row
        rightCellLocation.row == -1 -> leftCellLocation.row to rightCellLocation.row
        leftCellLocation.row < rightCellLocation.row -> leftCellLocation.row to rightCellLocation.row
        else -> rightCellLocation.row to leftCellLocation.row
    }

    val (minColumn, maxColumn) = when {
        leftCellLocation.row == -1 -> rightCellLocation.row to leftCellLocation.row
        rightCellLocation.row == -1 -> leftCellLocation.row to rightCellLocation.row
        leftCellLocation.row < rightCellLocation.row -> leftCellLocation.row to rightCellLocation.row
        else -> rightCellLocation.row to leftCellLocation.row
    }

    TODO()
}

private data class CellLocation(val row: Int, val column: Int)

private fun parseCellLocation(text: String): CellLocation {
    val reader = Reader(text)

    var column = -1

    while (isLetter(reader.c)) {
        val value = reader.c.uppercaseChar() - 'A'
        if (column != -1) {
            column *= 26
            column += value
        } else {
            column = value
        }

        reader.nextChar()
    }

    var row = -1

    while (isDigit(reader.c)) {
        val value = reader.c - '9'
        if (row != -1) {
            row *= 10
            row += value
        } else {
            row = value
        }

        reader.nextChar()
    }

    return CellLocation(row, column)
}

private fun isLetter(c: Char): Boolean {
    return c in 'a'..'z' || c in 'A'..'Z'
}

private fun isDigit(c: Char): Boolean {
    return c in '0'..'9'
}
