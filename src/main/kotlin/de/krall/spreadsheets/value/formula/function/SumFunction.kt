package de.krall.spreadsheets.value.formula.function

import de.krall.spreadsheets.value.ComputedValue
import de.krall.spreadsheets.value.formula.ReferenceResolver

object SumFunction : AbstractFunction() {

    override fun call(arguments: List<ComputedValue?>, references: ReferenceResolver): ComputedValue {
        val values = arguments.asSequence()
            .flatMap { resolveAll(it, references) }

        var sum = 0.0
        for (value in values) {
            val number = when (value) {
                is ComputedValue.Text -> 0.0
                is ComputedValue.Number -> value.number
                is ComputedValue.Reference -> 0.0
                is ComputedValue.ReferenceRange -> 0.0
                is ComputedValue.Error -> return value
            }

            sum += number
        }
        return ComputedValue.Number(sum)
    }
}
