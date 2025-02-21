package de.krall.spreadsheets.sheet.value

import de.krall.spreadsheets.sheet.value.parser.SlSource
import de.krall.spreadsheets.sheet.value.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.sheet.value.parser.tree.SlExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlFormulaStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.sheet.value.parser.tree.SlInvalid
import de.krall.spreadsheets.sheet.value.parser.tree.SlLiteral
import de.krall.spreadsheets.sheet.value.parser.tree.SlNumberStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.sheet.value.parser.tree.SlReference
import de.krall.spreadsheets.sheet.value.parser.tree.SlStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlTextStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlVisitor
import de.krall.spreadsheets.sheet.value.parser.type.Type

class TreeDumper(val buffer: StringBuilder) : SlVisitor<Int, Unit>() {

    override fun visitElement(element: SlElement, data: Int) {
        error("unhandled element: $element")
    }

    override fun visitStatement(statement: SlStatement, data: Int) {
        error("unhandled statement: $statement")
    }

    override fun visitTextStatement(statement: SlTextStatement, data: Int) {
        buffer.indent(data)
            .element("TEXT_STATEMENT", statement.source)
            .append(" ")
            .append("'")
            .append(statement.text)
            .append("'")
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitNumberStatement(statement: SlNumberStatement, data: Int) {
        buffer.indent(data)
            .element("NUMBER_STATEMENT", statement.source)
            .append(" ")
            .append("'")
            .append(statement.number)
            .append("'")
            .appendLine()

        statement.acceptChildren(this, data + 2)
    }

    override fun visitFormulaStatement(statement: SlFormulaStatement, data: Int) {
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
            .append(" ")
            .append("'")
            .append(literal.value)
            .append("'")
            .append(" ")
            .type(literal.typeOrNull)
            .appendLine()

        literal.acceptChildren(this, data + 2)
    }

    override fun visitReference(reference: SlReference, data: Int) {
        buffer.indent(data)
            .element("REFERENCE", reference.source)
            .append(" ")
            .append("'")
            .append(reference.leftName)
            .append("'")
        reference.rightName?.let { rightName ->
            buffer.append(" ")
                .append("'")
                .append(rightName)
                .append("'")
        }
        buffer.append(" ")
            .type(reference.typeOrNull)
            .append(" ")

        val referencing = reference.referencingOrNull
        if (referencing != null) {
            buffer.append("#")
                .append(referencing)
        } else {
            buffer.append("#unresolved")
        }

        buffer.appendLine()

        reference.acceptChildren(this, data + 2)
    }

    override fun visitInvalid(invalid: SlInvalid, data: Int) {
        buffer.indent(data)
            .element("INVALID", invalid.source)
            .append(" ")
            .type(invalid.typeOrNull)
            .appendLine()

        invalid.acceptChildren(this, data + 2)
    }

    override fun visitBinaryExpression(expression: SlBinaryExpression, data: Int) {
        buffer.indent(data)
            .element("BINARY_EXPRESSION", expression.source)
            .append(" ")
            .append(expression.operator)
            .append(" ")
            .type(expression.typeOrNull)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitPrefixExpression(expression: SlPrefixExpression, data: Int) {
        buffer.indent(data)
            .element("PREFIX_EXPRESSION", expression.source)
            .append(" ")
            .append(expression.operator)
            .append(" ")
            .type(expression.typeOrNull)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: Int) {
        buffer.indent(data)
            .element("PARENTHESIZED_EXPRESSION", expression.source)
            .append(" ")
            .type(expression.typeOrNull)
            .appendLine()

        expression.acceptChildren(this, data + 2)
    }

    override fun visitFunctionCall(functionCall: SlFunctionCall, data: Int) {
        buffer.indent(data)
            .element("FUNCTION_CALL", functionCall.source)
            .append(" ")
            .append("'")
            .append(functionCall.name)
            .append("'")
            .append(" ")
            .type(functionCall.typeOrNull)
            .append(" ")

        val function = functionCall.functionOrNull
        if (function != null) {
            buffer.append("#")
                .append(function)
        } else {
            buffer.append("#unresolved")
        }

        buffer.appendLine()

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
            append("(").append(source.offset).append("-").append(source.offset + source.length).append(")")
        } else {
            append("(-)")
        }
        return this
    }

    private fun StringBuilder.type(type: Type?): StringBuilder {
        append("<")
        if (type != null) {
            append(type)
        } else {
            append("unresolved")
        }
        append(">")
        return this
    }
}

