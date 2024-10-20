package de.krall.spreadsheets.sheet.value.parser.tree

abstract class SlVisitor<in D, out R> {

    abstract fun visitElement(element: SlElement, data: D): R

    open fun visitStatement(statement: SlStatement, data: D): R = visitElement(statement, data)

    open fun visitTextStatement(statement: SlTextStatement, data: D): R = visitStatement(statement, data)

    open fun visitNumberStatement(statement: SlNumberStatement, data: D): R = visitStatement(statement, data)

    open fun visitFormulaStatement(statement: SlFormulaStatement, data: D): R = visitStatement(statement, data)

    open fun visitExpression(expression: SlExpression, data: D): R = visitElement(expression, data)

    open fun visitLiteral(literal: SlLiteral, data: D): R = visitExpression(literal, data)

    open fun visitReference(reference: SlReference, data: D): R = visitExpression(reference, data)

    open fun visitInvalid(invalid: SlInvalid, data: D): R = visitExpression(invalid, data)

    open fun visitBinaryExpression(expression: SlBinaryExpression, data: D): R = visitExpression(expression, data)

    open fun visitPrefixExpression(expression: SlPrefixExpression, data: D): R = visitExpression(expression, data)

    open fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: D): R = visitExpression(expression, data)

    open fun visitFunctionCall(functionCall: SlFunctionCall, data: D): R = visitExpression(functionCall, data)
}
