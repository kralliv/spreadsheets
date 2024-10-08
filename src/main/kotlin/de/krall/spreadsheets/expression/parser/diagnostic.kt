package de.krall.spreadsheets.expression.parser

class Diagnostic(val severity: Severity, val message: String, val segment: Segment) {

    override fun toString(): String = buildString {
        append(severity)
        append(": ")
        append(message)
        append(" at ")
        append(segment.offset)
        if (segment.isNotEmpty()){
            append(" '")
            append(segment)
            append("'")
        }
    }
}

enum class Severity {
    WARNING,
    ERROR,
}

abstract class DiagnosticFactory

class DiagnosticFactory0(val severity: Severity, val message: String) : DiagnosticFactory() {

    fun at(segment: Segment): Diagnostic {
        return Diagnostic(severity, message, segment)
    }
}

fun interface DiagnosticSink {
    fun report(diagnostic: Diagnostic)
}
