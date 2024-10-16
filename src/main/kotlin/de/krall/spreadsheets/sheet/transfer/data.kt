package de.krall.spreadsheets.sheet.transfer

import de.krall.spreadsheets.value.Value

class TransferableSpreadsheet private constructor(
    val columnCount: Int,
    val rowCount: Int,
    val cells: List<TransferableCell>,
) {

    operator fun get(row: Int, column: Int): TransferableCell = cells[row * columnCount + column]

    override fun toString(): String = "TransferableSection(${columnCount}x$rowCount)"

    companion object {

        fun fromRows(rows: List<List<TransferableCell>>): TransferableSpreadsheet {
            val columnCount = rows.maxOfOrNull { it.size } ?: 0
            val rowCount = rows.size

            val cells = ArrayList<TransferableCell>(columnCount * rowCount)
            for (row in rows) {
                cells.addAll(row)
                // Pad the end
                repeat(columnCount - row.size) {
                    cells.add(TransferableCell.Blank)
                }
            }
            return TransferableSpreadsheet(columnCount, rowCount, cells)
        }
    }
}

class TransferableCell(
    val value: Value?,
    val renderedValue: String?,
) {

    override fun toString(): String = "TransferableCell('$renderedValue')"

    companion object {
        val Blank = TransferableCell(null, null)
    }
}
