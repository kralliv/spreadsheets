package de.krall.spreadsheets.value.parser.builder

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.ReferenceRange
import de.krall.spreadsheets.value.Referencing
import de.krall.spreadsheets.value.formula.Expression
import de.krall.spreadsheets.value.formula.Formula
import de.krall.spreadsheets.value.formula.FunctionCall
import de.krall.spreadsheets.value.formula.NumberConstant
import de.krall.spreadsheets.value.formula.ReferenceExpression
import de.krall.spreadsheets.value.formula.ReferenceRangeExpression
import de.krall.spreadsheets.value.formula.TextConstant
import de.krall.spreadsheets.value.formula.function.AddFunction
import de.krall.spreadsheets.value.formula.function.DereferenceFunction
import de.krall.spreadsheets.value.formula.function.DivideFunction
import de.krall.spreadsheets.value.formula.function.MinusFunction
import de.krall.spreadsheets.value.formula.function.ModuloFunction
import de.krall.spreadsheets.value.formula.function.MultiplyFunction
import de.krall.spreadsheets.value.formula.function.NegateFunction
import de.krall.spreadsheets.value.parser.analysis.function
import de.krall.spreadsheets.value.parser.analysis.referencing
import de.krall.spreadsheets.value.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlExpression
import de.krall.spreadsheets.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.value.parser.tree.SlInvalid
import de.krall.spreadsheets.value.parser.tree.SlLiteral
import de.krall.spreadsheets.value.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.value.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlVisitor

object FormulaBuilder {

    fun build(formula: SlExpression): Formula {
        val expressionBuilder = ExpressionBuilder()
        val expression = formula.accept(expressionBuilder, State.Initial)
        val references = expressionBuilder.finish()

        return Formula(expression, references)
    }

    private class ExpressionBuilder : SlVisitor<State, Expression>() {

        private val references = mutableListOf<Referencing>()

        override fun visitElement(element: SlElement, data: State): Expression {
            error("unsupported element $element")
        }

        override fun visitExpression(expression: SlExpression, data: State): Expression {
            error("unsupported expression $expression")
        }

        override fun visitLiteral(literal: SlLiteral, data: State): Expression {
            return when (literal.value) {
                is String -> TextConstant(literal.value)
                is Double -> NumberConstant(literal.value)
                else -> error("unsupported value type: ${literal.value}")
            }
        }

        override fun visitReference(reference: SlReference, data: State): Expression {
            references.add(reference.referencing)

            var expression = when (val referencing = reference.referencing) {
                is Reference -> ReferenceExpression(referencing)
                is ReferenceRange -> ReferenceRangeExpression(referencing)
            }

            if (!data.inFunctionArguments) {
                expression = FunctionCall(DereferenceFunction, listOf(expression))
            }

            return expression
        }

        override fun visitInvalid(invalid: SlInvalid, data: State): Expression {
            error("invalid should not be present")
        }

        override fun visitBinaryExpression(expression: SlBinaryExpression, data: State): Expression {
            val left = expression.left.accept(this, data)
            val right = expression.right.accept(this, data)

            val function = when (expression.operator) {
                SlBinaryExpression.Operator.PLUS -> AddFunction
                SlBinaryExpression.Operator.MINUS -> MinusFunction
                SlBinaryExpression.Operator.TIMES -> MultiplyFunction
                SlBinaryExpression.Operator.DIVIDE -> DivideFunction
                SlBinaryExpression.Operator.MODULO -> ModuloFunction
            }

            return FunctionCall(function, listOf(left, right))
        }

        override fun visitPrefixExpression(expression: SlPrefixExpression, data: State): Expression {
            val only = expression.expression.accept(this, data)

            val function = when (expression.operator) {
                SlPrefixExpression.Operator.PLUS -> return only
                SlPrefixExpression.Operator.MINUS -> NegateFunction
            }

            return FunctionCall(function, listOf(only))
        }

        override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: State): Expression {
            return expression.expression.accept(this, data)
        }

        override fun visitFunctionCall(functionCall: SlFunctionCall, data: State): Expression {
            val argumentsState = data.copy(inFunctionArguments = true)
            val arguments = functionCall.arguments.map { it.accept(this, argumentsState) }

            return FunctionCall(functionCall.function.function, arguments)
        }

        fun finish(): List<Referencing> {
            return references.toList()
        }
    }

    private data class State(
        val inFunctionArguments: Boolean,
    ) {
        companion object {
            val Initial = State(
                inFunctionArguments = false
            )
        }
    }
}
