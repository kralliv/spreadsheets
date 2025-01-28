package de.krall.spreadsheets.sheet.value.parser.analysis

import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange
import de.krall.spreadsheets.sheet.value.parser.ProcessingContext
import de.krall.spreadsheets.sheet.value.parser.diagnotic.Diagnostics
import de.krall.spreadsheets.sheet.value.parser.parseOrNull
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.sheet.value.parser.tree.SlReference
import de.krall.spreadsheets.sheet.value.parser.tree.SlVisitorVoid

object ReferenceResolution : TreeAnalyser {

    override fun check(element: SlElement, context: ProcessingContext) {
        element.accept(object : SlVisitorVoid() {
            override fun visitElement(element: SlElement) {
                element.acceptChildren(this)
            }

            override fun visitReference(reference: SlReference) {
                if (reference.rightName == null) {
                    val resolvedReference = Reference.parseOrNull(reference.leftName)

                    if (resolvedReference == null) {
                        context.report(Diagnostics.INVALID_REFERENCE.on(reference, reference.leftName))
                        return
                    }

                    reference.referencingOrNull = resolvedReference
                } else {
                    val resolvedReferenceRange = ReferenceRange.parseOrNull(reference.leftName, reference.rightName)

                    if (resolvedReferenceRange == null) {
                        context.report(Diagnostics.INVALID_REFERENCE_RANGE.on(reference, "${reference.leftName}:${reference.rightName}"))
                    }

                    reference.referencingOrNull = resolvedReferenceRange
                }
            }
        })
    }
}
