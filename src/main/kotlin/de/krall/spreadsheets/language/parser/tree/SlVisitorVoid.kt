package de.krall.spreadsheets.language.parser.tree

abstract class SlVisitorVoid : SlVisitor<Nothing?, Unit>() {

    final override fun visitElement(element: SlElement, data: Nothing?) {
        visitElement(element)
    }

    abstract fun visitElement(element: SlElement)

    final override fun visitStatement(statement: SlStatement, data: Nothing?) {
        visitStatement(statement)
    }

    open fun visitStatement(statement: SlStatement) = visitElement(statement)

    final override fun visitTextStatement(statement: SlTextStatement, data: Nothing?) {
        visitTextStatement(statement)
    }

    open fun visitTextStatement(statement: SlTextStatement) = visitStatement(statement)

    final override fun visitNumberStatement(statement: SlNumberStatement, data: Nothing?) {
        visitNumberStatement(statement)
    }

    open fun visitNumberStatement(statement: SlNumberStatement) = visitStatement(statement)

    final override fun visitFormulaStatement(statement: SlFormulaStatement, data: Nothing?) {
        visitFormulaStatement(statement)
    }

    open fun visitFormulaStatement(statement: SlFormulaStatement) = visitStatement(statement)

    final override fun visitExpression(expression: SlExpression, data: Nothing?) {
        visitExpression(expression)
    }

    fun visitExpression(expression: SlExpression) = visitElement(expression)

    final override fun visitLiteral(expression: SlLiteral, data: Nothing?) {
        visitLiteral(expression)
    }

    open fun visitLiteral(expression: SlLiteral) = visitExpression(expression)

    final override fun visitReference(reference: SlReference, data: Nothing?) {
        visitReference(reference)
    }

    open fun visitReference(expression: SlReference) = visitExpression(expression)

    final override fun visitInvalid(invalid: SlInvalid, data: Nothing?) {
        visitInvalid(invalid)
    }

    open fun visitInvalid(expression: SlInvalid) = visitExpression(expression)

    final override fun visitBinaryExpression(expression: SlBinaryExpression, data: Nothing?) {
        visitBinaryExpression(expression)
    }

    open fun visitBinaryExpression(expression: SlBinaryExpression) = visitExpression(expression)

    final override fun visitPrefixExpression(expression: SlPrefixExpression, data: Nothing?) {
        visitPrefixExpression(expression)
    }

    open fun visitPrefixExpression(expression: SlPrefixExpression) = visitExpression(expression)

    final override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: Nothing?) {
        visitParenthesizedExpression(expression)
    }

    open fun visitParenthesizedExpression(expression: SlParenthesizedExpression) = visitExpression(expression)

    final override fun visitFunctionCall(functionCall: SlFunctionCall, data: Nothing?) {
        visitFunctionCall(functionCall)
    }

    open fun visitFunctionCall(expression: SlFunctionCall) = visitExpression(expression)
}
