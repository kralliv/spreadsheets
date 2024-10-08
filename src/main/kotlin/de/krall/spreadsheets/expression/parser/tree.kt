package de.krall.spreadsheets.expression.parser

import java.math.BigDecimal

class Location(val offset: Int, val length: Int) {

    override fun toString(): String = "($offset to ${offset + length})"
}

interface Element {

    val location: Location?
}

abstract class AbstractElement : Element {

    override fun toString(): String = "${this::class.simpleName}"
}

abstract class Statement : AbstractElement() {

}

class TextStatement(
    val text: String,
    override val location: Location? = null,
) : Statement() {

}

class NumberStatement(
    val number: BigDecimal,
    override val location: Location? = null,
) : Statement() {

}

class FormulaStatement(
    val expression: Expression,
    override val location: Location? = null,
) : Statement() {

}

abstract class Expression : AbstractElement() {

}

class LiteralExpression(
    val value: Any?,
    override val location: Location? = null,
) : Expression() {

}

class ReferenceExpression(
    val name: String,
    override val location: Location? = null,
) : Expression() {

}

class InvalidExpression(
    override val location: Location? = null,
) : Expression() {

}

class BinaryExpression(
    val left: Expression,
    val operator: Operator,
    val right: Expression,
    override val location: Location? = null,
) : Expression() {

    enum class Operator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDE,
        MODULO,
    }
}

class PrefixExpression(
    val operator: Operator,
    val expression: Expression,
    override val location: Location? = null,
) : Expression() {

    enum class Operator {
        PLUS,
        MINUS,
    }
}

class ParenthesizedExpression(
    val expression: Expression,
    override val location: Location? = null,
) : Expression() {

}

class FunctionCallExpression(
    val name: String,
    val arguments: List<Expression>,
    override val location: Location? = null,
) : Expression() {

}
