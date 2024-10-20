package de.krall.spreadsheets.sheet.value.formula.function

import de.krall.spreadsheets.sheet.value.ComputedValue
import de.krall.spreadsheets.sheet.value.formula.ReferenceResolver

object SumFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue>, references: ReferenceResolver): ComputedValue {
        val values = arguments.asSequence()
            .flatMap { it.dereferenceAll(references) }

        var sum = 0.0
        for (value in values) {
            val number = number(value) ?: return value

            sum += number
        }
        return ComputedValue.Number(sum)
    }
}
