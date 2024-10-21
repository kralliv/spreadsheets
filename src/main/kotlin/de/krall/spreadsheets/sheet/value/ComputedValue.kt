package de.krall.spreadsheets.sheet.value

sealed class ComputedValue {
    data object Blank : ComputedValue()
    data class Text(val text: String) : ComputedValue()
    data class Number(val number: Double) : ComputedValue()
    data class Reference(val reference: de.krall.spreadsheets.sheet.value.Reference) : ComputedValue()
    data class ReferenceRange(val referenceRange: de.krall.spreadsheets.sheet.value.ReferenceRange) : ComputedValue()
    data class Error(val error: ComputationError) : ComputedValue()
}

sealed class ComputationError {
    data object BadFormula : ComputationError()
    data object CircularDependency : ComputationError()
    data object DivisionByZero : ComputationError()
    data object Error : ComputationError()
}
