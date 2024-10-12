package de.krall.spreadsheets.value

import de.krall.spreadsheets.value.parser.SlSource
import de.krall.spreadsheets.value.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlExpression
import de.krall.spreadsheets.value.parser.tree.SlFormulaValue
import de.krall.spreadsheets.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.value.parser.tree.SlInvalid
import de.krall.spreadsheets.value.parser.tree.SlLiteral
import de.krall.spreadsheets.value.parser.tree.SlNumberValue
import de.krall.spreadsheets.value.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.value.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlValue
import de.krall.spreadsheets.value.parser.tree.SlTextValue
import de.krall.spreadsheets.value.parser.tree.SlVisitor

class TreeDumper(val buffer: StringBuilder) : SlVisitor<Int, Unit>() {

    override fun visitElement(element: SlElement, data: Int) {
        error("unhandled element: $element")
    }

    override fun visitStatement(statement: SlValue, data: Int) {
        error("unhandled statement: $statement")
    }

    override fun visitTextStatement(statement: SlTextValue, data: Int) {
        buffer.indent(data)
            .element("TEXT_STATEMENT", statement.source)
            .append("'")
            .append(statement.text)
            .append("'")
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitNumberStatement(statement: SlNumberValue, data: Int) {
        buffer.indent(data)
            .element("NUMBER_STATEMENT", statement.source)
            .append(statement.number)
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitFormulaStatement(statement: SlFormulaValue, data: Int) {
        buffer.indent(data)
            .element("FORMULA_STATEMENT", statement.source)
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitExpression(expression: SlExpression, data: Int) {
        error("unhandled expression: $expression")
    }

    override fun visitLiteral(literal: SlLiteral, data: Int) {
        buffer.indent(data)
            .element("LITERAL", literal.source)
            .append(literal.value)
            .appendLine()

        literal.acceptChildren(this, data + 2)
    }

    override fun visitReference(reference: SlReference, data: Int) {
        buffer.indent(data)
            .element("REFERENCE", reference.source)
            .append("'")
            .append(reference.leftName)
            .append("'")
        reference.rightName?.let { rightName ->
            buffer.append(" ")
                .append("'")
                .append(reference.leftName)
                .append("'")
        }
        buffer.appendLine()

        reference.acceptChildren(this, data + 2)
    }

    override fun visitInvalid(invalid: SlInvalid, data: Int) {
        buffer.indent(data)
            .element("INVALID", invalid.source)
            .appendLine()

        invalid.acceptChildren(this, data + 2)
    }

    override fun visitBinaryExpression(expression: SlBinaryExpression, data: Int) {
        buffer.indent(data)
            .element("BINARY_EXPRESSION", expression.source)
            .append(expression.operator)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitPrefixExpression(expression: SlPrefixExpression, data: Int) {
        buffer.indent(data)
            .element("PREFIX_EXPRESSION", expression.source)
            .append(expression.operator)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: Int) {
        buffer.indent(data)
            .element("PARENTHESIZED_EXPRESSION", expression.source)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitFunctionCall(functionCall: SlFunctionCall, data: Int) {
        buffer.indent(data)
            .element("FUNCTION_CALL", functionCall.source)
            .append("'")
            .append(functionCall.name)
            .append("'")
            .appendLine()

        functionCall.acceptChildren(this, data + 2)
    }

    private fun StringBuilder.indent(indent: Int): StringBuilder {
        repeat(indent) {
            append(' ')
        }
        return this
    }

    private fun StringBuilder.element(name: String, source: SlSource?): StringBuilder {
        append(name)
        if (source != null) {
            append("(").append(source.offset).append("-").append(source.offset + source.length).append(") ")
        } else {
            append("(-)")
        }
        return this
    }
}

