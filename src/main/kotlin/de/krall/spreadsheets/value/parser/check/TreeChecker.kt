package de.krall.spreadsheets.value.parser.check

import de.krall.spreadsheets.value.parser.ProcessingContext
import de.krall.spreadsheets.value.parser.tree.SlElement

interface TreeChecker {

    fun check(tree: SlElement, context: ProcessingContext)
}
