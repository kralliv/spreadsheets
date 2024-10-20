package de.krall.spreadsheets.sheet.transfer

import de.krall.spreadsheets.util.parseCsv
import de.krall.spreadsheets.sheet.value.Value

fun TransferableSpreadsheet.toCsv(): String {
    return buildString {
        for (row in 0..<rowCount) {
            if (row > 0) appendLine()

            for (column in 0..<columnCount) {
                if (column > 0) append(",")

                val cell = get(row, column)

                val value = cell.renderedValue ?: ""
                val escape = value.contains(',') || value.contains('"')

                if (escape) {
                    append('"')
                    append(value.replace("\"", "\"\""))
                    append('"')
                } else {
                    append(value)
                }
            }
        }
    }
}

fun TransferableSpreadsheet.Companion.fromCsv(csv: String): TransferableSpreadsheet {
    val rows = csv.parseCsv().map { row ->
        row.map { entry ->
            val renderedValue = entry.ifEmpty { null }
            val value = renderedValue?.let { Value.Text(it) }

            TransferableCell(
                value,
                renderedValue,
            )
        }
    }.toList()

    return fromRows(rows)
}
