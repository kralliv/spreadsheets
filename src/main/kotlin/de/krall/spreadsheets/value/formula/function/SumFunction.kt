package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

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
