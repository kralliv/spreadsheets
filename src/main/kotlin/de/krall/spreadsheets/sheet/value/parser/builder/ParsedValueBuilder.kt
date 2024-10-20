package de.krall.spreadsheets.sheet.value.parser.builder

import de.krall.spreadsheets.sheet.value.ParsedValue
import de.krall.spreadsheets.sheet.value.parser.tree.SlElement
import de.krall.spreadsheets.sheet.value.parser.tree.SlFormulaStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlNumberStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlTextStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlStatement
import de.krall.spreadsheets.sheet.value.parser.tree.SlVisitor

object ParsedValueBuilder {

    fun build(value: SlStatement): ParsedValue {
        return value.accept(Builder, Unit)
    }

    private object Builder : SlVisitor<Unit, ParsedValue>() {

        override fun visitElement(element: SlElement, data: Unit): ParsedValue {
            error("unsupported element $element")
        }

        override fun visitStatement(statement: SlStatement, data: Unit): ParsedValue {
            error("unsupported value $statement")
        }

        override fun visitTextStatement(statement: SlTextStatement, data: Unit): ParsedValue {
            return ParsedValue.Text(statement.text)
        }

        override fun visitNumberStatement(statement: SlNumberStatement, data: Unit): ParsedValue {
            return ParsedValue.Number(statement.number)
        }

        override fun visitFormulaStatement(statement: SlFormulaStatement, data: Unit): ParsedValue {
            val formula = FormulaBuilder.build(statement.expression)

            return ParsedValue.Formula(formula)
        }
    }
}
