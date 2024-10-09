package de.krall.spreadsheets.language.parser.check

import de.krall.spreadsheets.language.parser.ProcessingContext
import de.krall.spreadsheets.language.parser.tree.SlStatement

interface TreeChecker {

    fun check(statement: SlStatement, context: ProcessingContext)
}
