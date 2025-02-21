package de.krall.spreadsheets.sheet.value.parser.builder

import de.krall.spreadsheets.sheet.value.Reference
import de.krall.spreadsheets.sheet.value.ReferenceRange
import de.krall.spreadsheets.sheet.value.formula.*
import de.krall.spreadsheets.sheet.value.formula.function.*
import de.krall.spreadsheets.sheet.value.parser.analysis.function
import de.krall.spreadsheets.sheet.value.parser.analysis.referencing
import de.krall.spreadsheets.sheet.value.parser.tree.*

object FormulaBuilder {

    fun build(formula: SlExpression): Formula {
        val expressionBuilder = ExpressionBuilder()
        val expression = formula.accept(expressionBuilder, State.Initial)
        return Formula(expression)
    }

    private class ExpressionBuilder : SlVisitor<State, Expression>() {

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
            var expression = when (val referencing = reference.referencing) {
                is Reference -> ReferenceExpression(referencing)
                is ReferenceRange -> ReferenceRangeExpression(referencing)
            }

            if (reference.referencing is Reference && !data.inFunctionArguments) {
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
