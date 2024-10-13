package de.krall.spreadsheets.value.parser.analysis

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.ReferenceRange
import de.krall.spreadsheets.value.parser.ProcessingContext
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostics
import de.krall.spreadsheets.value.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlFormulaStatement
import de.krall.spreadsheets.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.value.parser.tree.SlInvalid
import de.krall.spreadsheets.value.parser.tree.SlLiteral
import de.krall.spreadsheets.value.parser.tree.SlNumberStatement
import de.krall.spreadsheets.value.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.value.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlStatement
import de.krall.spreadsheets.value.parser.tree.SlTextStatement
import de.krall.spreadsheets.value.parser.tree.SlVisitor
import de.krall.spreadsheets.value.parser.type.BuiltIns
import de.krall.spreadsheets.value.parser.type.FunctionDefinition
import de.krall.spreadsheets.value.parser.type.ParameterTypes
import de.krall.spreadsheets.value.parser.type.Type

object TypeResolver : TreeAnalyser {

    override fun check(tree: SlElement, context: ProcessingContext) {
        tree.accept(ExpressionTypeResolver(context), Unit)
    }

    private class ExpressionTypeResolver(val context: ProcessingContext) : SlVisitor<Unit, Type>() {

        override fun visitElement(element: SlElement, data: Unit): Type {
            error("element is not an expression: $element")
        }

        override fun visitStatement(statement: SlStatement, data: Unit): Type {
            error("unsupported statement: $statement")
        }

        override fun visitTextStatement(statement: SlTextStatement, data: Unit): Type = BuiltIns.Nothing

        override fun visitNumberStatement(statement: SlNumberStatement, data: Unit): Type = BuiltIns.Nothing

        override fun visitFormulaStatement(statement: SlFormulaStatement, data: Unit): Type {
            statement.expression.accept(this, data)

            return BuiltIns.Nothing
        }

        override fun visitLiteral(literal: SlLiteral, data: Unit): Type {
            val type = when (literal.value) {
                is String -> BuiltIns.Text
                is Double -> BuiltIns.Number
                else -> error("unsupported literal value type ${literal.value}")
            }
            literal.typeOrNull = type
            return type
        }

        override fun visitReference(reference: SlReference, data: Unit): Type {
            val type = when (reference.referencingOrNull) {
                null -> BuiltIns.Reference // FIXME
                is Reference -> BuiltIns.Reference
                is ReferenceRange -> BuiltIns.ReferenceRange
            }
            reference.typeOrNull = type
            return type
        }

        override fun visitInvalid(invalid: SlInvalid, data: Unit): Type {
            val type = BuiltIns.Error
            invalid.typeOrNull = type
            return type
        }

        override fun visitBinaryExpression(expression: SlBinaryExpression, data: Unit): Type {
            val inputType = when (expression.operator) {
                SlBinaryExpression.Operator.PLUS,
                SlBinaryExpression.Operator.MINUS,
                SlBinaryExpression.Operator.TIMES,
                SlBinaryExpression.Operator.DIVIDE,
                SlBinaryExpression.Operator.MODULO,
                    -> BuiltIns.AnySingular
            }

            val leftType = expression.left.accept(this, data)
            if (leftType != BuiltIns.Error && !inputType.isAssignableFrom(leftType)) {
                context.report(Diagnostics.TYPE_MISMATCH.on(expression.left, inputType, leftType))
            }

            val rightType = expression.right.accept(this, data)
            if (rightType != BuiltIns.Error && !inputType.isAssignableFrom(rightType)) {
                context.report(Diagnostics.TYPE_MISMATCH.on(expression.right, inputType, rightType))
            }

            val returnType = when (expression.operator) {
                SlBinaryExpression.Operator.PLUS,
                SlBinaryExpression.Operator.MINUS,
                SlBinaryExpression.Operator.TIMES,
                SlBinaryExpression.Operator.DIVIDE,
                SlBinaryExpression.Operator.MODULO,
                    -> BuiltIns.Number
            }
            expression.typeOrNull = returnType
            return returnType
        }

        override fun visitPrefixExpression(expression: SlPrefixExpression, data: Unit): Type {
            val inputType = when (expression.operator) {
                SlPrefixExpression.Operator.PLUS,
                SlPrefixExpression.Operator.MINUS,
                    -> BuiltIns.AnySingular
            }

            val onlyType = expression.expression.accept(this, data)
            if (onlyType != BuiltIns.Error && !inputType.isAssignableFrom(onlyType)) {
                context.report(Diagnostics.TYPE_MISMATCH.on(expression.expression, inputType, onlyType))
            }

            val returnType = when (expression.operator) {
                SlPrefixExpression.Operator.PLUS,
                SlPrefixExpression.Operator.MINUS,
                    -> BuiltIns.Number
            }
            expression.typeOrNull = returnType
            return returnType
        }

        override fun visitParenthesizedExpression(expression: SlParenthesizedExpression, data: Unit): Type {
            val type = expression.expression.accept(this, data)
            expression.typeOrNull = type
            return type
        }

        override fun visitFunctionCall(functionCall: SlFunctionCall, data: Unit): Type {
            val functionName = functionCall.name.lowercase()
            val functionCandidates = BuiltIns.functions.filter { it.name == functionName }

            if (functionCandidates.isEmpty()) {
                context.report(Diagnostics.UNKNOWN_FUNCTION.on(functionCall, functionCall.name))
                return BuiltIns.Error
            }

            val argumentTypes = functionCall.arguments.map { it.accept(this, data) }

            val function = functionCandidates.find { it.parameterTypes.matches(argumentTypes) }

            if (function == null) {
                val functionCandidate = functionCandidates.maxBy { it.parameterTypes.score(argumentTypes) }
                reportArgumentMismatches(functionCall, argumentTypes, functionCandidate)
                return BuiltIns.Error
            }

            functionCall.functionOrNull = function
            functionCall.typeOrNull = function.returnType
            return function.returnType
        }

        fun reportArgumentMismatches(
            functionCall: SlFunctionCall,
            argumentTypes: List<Type>,
            function: FunctionDefinition,
        ) {
            val arguments = functionCall.arguments.zip(argumentTypes)

            val iterator = arguments.iterator()

            for (parameterType in function.parameterTypes.fixed) {
                if (!iterator.hasNext()) {
                    val element = arguments.lastOrNull()?.first ?: functionCall
                    context.report(Diagnostics.MISSING_ARGUMENT.on(element, parameterType))
                    return
                }
                val (argument, argumentType) = iterator.next()
                if (argumentType != BuiltIns.Error && !parameterType.isAssignableFrom(argumentType)) {
                    context.report(Diagnostics.TYPE_MISMATCH.on(argument, parameterType, argumentType))
                }
            }

            function.parameterTypes.variadic?.let { parameterType ->
                while (iterator.hasNext()) {
                    val (argument, argumentType) = iterator.next()
                    if (argumentType != BuiltIns.Error && !parameterType.isAssignableFrom(argumentType)) {
                        context.report(Diagnostics.TYPE_MISMATCH.on(argument, parameterType, argumentType))
                    }
                }
            }

            while (iterator.hasNext()) {
                val (argument, _) = iterator.next()
                context.report(Diagnostics.TOO_MANY_ARGUMENTS.on(argument))
            }
        }
    }

    private fun ParameterTypes.matches(argumentTypes: List<Type>): Boolean {
        val iterator = argumentTypes.iterator()

        for (type in fixed) {
            if (!iterator.hasNext()) return false
            if (!type.isAssignableFrom(iterator.next())) return false
        }

        variadic?.let { parameterType ->
            while (iterator.hasNext()) {
                if (!parameterType.isAssignableFrom(iterator.next())) return false
            }
        }

        return !iterator.hasNext()
    }

    private fun ParameterTypes.score(argumentTypes: List<Type>): Int {
        val iterator = argumentTypes.iterator()

        var score = 0

        for (type in fixed) {
            if (!iterator.hasNext()) return score
            if (type.isAssignableFrom(iterator.next())) {
                score++
            }
        }

        variadic?.let { parameterType ->
            while (iterator.hasNext()) {
                if (parameterType.isAssignableFrom(iterator.next())) {
                    score++
                }
            }
        }

        return score
    }
}
