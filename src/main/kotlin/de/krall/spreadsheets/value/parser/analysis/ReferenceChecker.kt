package de.krall.spreadsheets.value.parser.analysis

import de.krall.spreadsheets.value.parser.ProcessingContext
import de.krall.spreadsheets.value.parser.Reader
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostics
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlVisitorVoid

object ReferenceChecker : TreeChecker {

    override fun check(element: SlElement, context: ProcessingContext) {
        element.accept(object : SlVisitorVoid() {
            override fun visitElement(element: SlElement) {
                element.acceptChildren(this)
            }

            override fun visitReference(reference: SlReference) {
                if (!isValidReference(reference.leftName)) {
                    context.report(Diagnostics.INVALID_REFERENCE.on(reference, reference.leftName))
                }
                if (reference.rightName != null && !isValidReference(reference.rightName)) {
                    context.report(Diagnostics.INVALID_REFERENCE.on(reference, reference.rightName))
                }
            }
        })
    }

    private fun isValidReference(name: String): Boolean {
        val reader = Reader(name)

        while (isLetter(reader.c)) {
            reader.nextChar()
        }

        while (isDigit(reader.c)) {
            reader.nextChar()
        }

        return reader.isEof()
    }

    private fun isLetter(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z'
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }
}
