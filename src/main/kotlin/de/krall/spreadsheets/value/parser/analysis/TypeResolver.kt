package de.krall.spreadsheets.value.parser.analysis

import de.krall.spreadsheets.value.Reference
import de.krall.spreadsheets.value.parser.ProcessingContext
import de.krall.spreadsheets.value.parser.diagnotic.Diagnostics
import de.krall.spreadsheets.value.parser.tree.SlBinaryExpression
import de.krall.spreadsheets.value.parser.tree.SlElement
import de.krall.spreadsheets.value.parser.tree.SlFunctionCall
import de.krall.spreadsheets.value.parser.tree.SlInvalid
import de.krall.spreadsheets.value.parser.tree.SlLiteral
import de.krall.spreadsheets.value.parser.tree.SlParenthesizedExpression
import de.krall.spreadsheets.value.parser.tree.SlPrefixExpression
import de.krall.spreadsheets.value.parser.tree.SlReference
import de.krall.spreadsheets.value.parser.tree.SlVisitorVoid
import de.krall.spreadsheets.value.parser.type.BuiltIns
import de.krall.spreadsheets.value.parser.type.FunctionDefinition
import de.krall.spreadsheets.value.parser.type.ParameterTypes
import de.krall.spreadsheets.value.parser.type.Type

object TypeResolver : TreeChecker {

    override fun check(tree: SlElement, context: ProcessingContext) {
        tree.accept(object : SlVisitorVoid() {

            override fun visitElement(element: SlElement) {
                element.acceptChildren(this)
            }

            override fun visitLiteral(literal: SlLiteral) {
                literal.typeOrNull = when (literal.value) {
                    is String -> BuiltIns.String
                    is Double -> BuiltIns.Number
                    else -> error("unsupported literal value type ${literal.value}")
                }
            }

            override fun visitReference(reference: SlReference) {

                val ref = reference.referenceOrNull ?: error("references should have been resolved")
                reference.typeOrNull = when (ref) {
                    is Reference.Cell -> BuiltIns.Reference
                    is Reference.Area -> BuiltIns.ReferenceRange
                }
            }

            override fun visitInvalid(invalid: SlInvalid) {
                invalid.typeOrNull = BuiltIns.Nothing
            }

            override fun visitBinaryExpression(expression: SlBinaryExpression) {
                expression.acceptChildren(this)

                expression.typeOrNull = when (expression.operator) {
                    SlBinaryExpression.Operator.PLUS,
                    SlBinaryExpression.Operator.MINUS,
                    SlBinaryExpression.Operator.TIMES,
                    SlBinaryExpression.Operator.DIVIDE,
                    SlBinaryExpression.Operator.MODULO,
                        -> BuiltIns.Number
                }
            }

            override fun visitPrefixExpression(expression: SlPrefixExpression) {
                expression.acceptChildren(this)

                expression.typeOrNull = when (expression.operator) {
                    SlPrefixExpression.Operator.PLUS,
                    SlPrefixExpression.Operator.MINUS,
                        -> BuiltIns.Number
                }
            }

            override fun visitParenthesizedExpression(expression: SlParenthesizedExpression) {
                expression.acceptChildren(this)

                expression.typeOrNull = expression.expression.type
            }

            override fun visitFunctionCall(functionCall: SlFunctionCall) {
                functionCall.acceptChildren(this)

                val functionName = functionCall.name.lowercase()
                val functionCandidates = BuiltIns.functions.filter { it.name == functionName }

                if (functionCandidates.isEmpty()) {
                    context.report(Diagnostics.UNKNOWN_FUNCTION.on(functionCall, functionCall.name))
                    return
                }

                val argumentTypes = functionCall.arguments.map { it.type }

                val function = functionCandidates.find { it.parameterTypes.matches(argumentTypes) }

                if (function == null) {
                    val functionCandidate = functionCandidates.maxBy { it.parameterTypes.score(argumentTypes) }
                    reportArgumentMismatches(functionCall, argumentTypes, functionCandidate)
                    return
                }

                functionCall.functionOrNull = function
                functionCall.typeOrNull = function.returnType
            }

            fun reportArgumentMismatches(functionCall: SlFunctionCall, argumentTypes: List<Type>, function: FunctionDefinition) {
                val iterator = argumentTypes.iterator()

                for (parameterType in function.parameterTypes.fixed) {
                    if (!iterator.hasNext()) return
                    val argumentType = iterator.next()
                    if (!parameterType.isAssignableFrom(argumentType)) {
                        context.report(Diagnostics.TYPE_MISMATCH.on(functionCall, parameterType, argumentType))
                    }
                }

                function.parameterTypes.variadic?.let { parameterType ->
                    while (iterator.hasNext()) {
                        val argumentType = iterator.next()
                        if (!parameterType.isAssignableFrom(argumentType)) {
                            context.report(Diagnostics.TYPE_MISMATCH.on(functionCall, parameterType, argumentType))
                        }
                    }
                }
            }
        })
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






