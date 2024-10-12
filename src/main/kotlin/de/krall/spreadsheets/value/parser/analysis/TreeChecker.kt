package de.krall.spreadsheets.value.parser.analysis

import de.krall.spreadsheets.value.parser.ProcessingContext
import de.krall.spreadsheets.value.parser.tree.SlElement

interface TreeChecker {

    fun check(tree: SlElement, context: ProcessingContext)
}
