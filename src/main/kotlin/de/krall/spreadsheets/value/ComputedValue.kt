package de.krall.spreadsheets.value

sealed class ComputedValue {
    data class Text(val text: String) : ComputedValue()
    data class Number(val number: Double) : ComputedValue()
    data class Reference(val reference: de.krall.spreadsheets.value.Reference.Cell) : ComputedValue()
    data class ReferenceRange(val reference: de.krall.spreadsheets.value.Reference.Area) : ComputedValue()
    data class Error(val error: ComputationError) : ComputedValue()
}

sealed class ComputationError {
    data object DivisionByZero : ComputationError()
}
