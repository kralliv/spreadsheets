package de.krall.spreadsheets.sheet.value.parser.analysis

import de.krall.spreadsheets.sheet.value.parser.ProcessingContext
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement

interface TreeAnalyser {

    fun check(tree: SlElement, context: ProcessingContext)
}
