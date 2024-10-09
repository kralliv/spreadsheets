package de.krall.spreadsheets.language

import de.krall.spreadsheets.language.parser.Location
import de.krall.spreadsheets.language.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.language.parser.tree.SlElement
import de.krall.spreadsheets.language.parser.tree.SlExpression
import de.krall.spreadsheets.language.parser.tree.SlFormulaStatement
import de.krall.spreadsheets.language.parser.tree.SlFunctionCall
import de.krall.spreadsheets.language.parser.tree.SlInvalid
import de.krall.spreadsheets.language.parser.tree.SlLiteral
import de.krall.spreadsheets.language.parser.tree.SlNumberStatement
import de.krall.spreadsheets.language.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.language.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.language.parser.tree.SlReference
import de.krall.spreadsheets.language.parser.tree.SlStatement
import de.krall.spreadsheets.language.parser.tree.SlTextStatement
import de.krall.spreadsheets.language.parser.tree.SlVisitor

class TreeDumper(val buffer: StringBuilder) : SlVisitor<Int, Unit>() {

    override fun visitElement(element: SlElement, data: Int) {
        error("unhandled element: $element")
    }

    override fun visitStatement(statement: SlStatement, data: Int) {
        error("unhandled statement: $statement")
    }

    override fun visitTextStatement(statement: SlTextStatement, data: Int) {
        buffer.indent(data)
            .element("TEXT_STATEMENT", statement.location)
            .append("'")
            .append(statement.text)
            .append("'")
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitNumberStatement(statement: SlNumberStatement, data: Int) {
        buffer.indent(data)
            .element("NUMBER_STATEMENT", statement.location)
            .append(statement.number)
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitFormulaStatement(statement: SlFormulaStatement, data: Int) {
        buffer.indent(data)
            .element("FORMULA_STATEMENT", statement.location)
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitExpression(expression: SlExpression, data: Int) {
        error("unhandled expression: $expression")
    }

    override fun visitLiteral(literal: SlLiteral, data: Int) {
        buffer.indent(data)
            .element("LITERAL", literal.location)
            .append(literal.value)
            .appendLine()

        literal.acceptChildren(this, data + 2)
    }

    override fun visitReference(reference: SlReference, data: Int) {
        buffer.indent(data)
            .element("REFERENCE", reference.location)
            .append(reference.name)
            .appendLine()

        reference.acceptChildren(this, data + 2)
    }

    override fun visitInvalid(invalid: SlInvalid, data: Int) {
        buffer.indent(data)
            .element("REFERENCE", invalid.location)
            .appendLine()

        invalid.acceptChildren(this, data + 2)
    }

    override fun visitBinaryExpression(expression: SlBinaryExpression, data: Int) {
        buffer.indent(data)
            .element("BINARY_EXPRESSION", expression.location)
            .append(expression.operator)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitPrefixExpression(expression: SlPrefixExpression, data: Int) {
        buffer.indent(data)
            .element("PREFIX_EXPRESSION", expression.location)
            .append(expression.operator)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: Int) {
        buffer.indent(data)
            .element("PARENTHESIZED_EXPRESSION", expression.location)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitFunctionCall(functionCall: SlFunctionCall, data: Int) {
        buffer.indent(data)
            .element("FUNCTION_CALL", functionCall.location)
            .append(functionCall.name)
            .appendLine()

        functionCall.acceptChildren(this, data + 2)
    }

    private fun StringBuilder.indent(indent: Int): StringBuilder {
        repeat(indent) {
            append(' ')
        }
        return this
    }

    private fun StringBuilder.element(name: String, location: Location?): StringBuilder {
        append(name)
        if (location != null) {
            append("(").append(location.offset).append("-").append(location.offset + location.length).append(") ")
        } else {
            append("(-)")
        }
        return this
    }
}

