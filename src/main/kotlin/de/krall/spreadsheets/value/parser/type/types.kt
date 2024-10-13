package de.krall.spreadsheets.value.parser.type

interface Type {

    fun isAssignableFrom(other: Type): Boolean
}

abstract class AbstractType : Type

object AnyType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = true

    override fun toString(): String = "any"
}

object TextType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = other == this

    override fun toString(): String = "text"
}

object NumberType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = other == this

    override fun toString(): String = "number"
}

object ReferenceType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = other == this

    override fun toString(): String = "reference"
}

object ReferenceRangeType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = other == this

    override fun toString(): String = "reference-range"
}

object NothingType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = false

    override fun toString(): String = "nothing"
}

object ErrorType : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = false

    override fun toString(): String = "error"
}

fun UnionType(vararg types: Type): UnionType = UnionType(types.toSet())

class UnionType(val types: Set<Type>) : AbstractType() {

    override fun isAssignableFrom(other: Type): Boolean = types.any { it.isAssignableFrom(other) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnionType) return false
        return types == other.types
    }

    override fun hashCode(): Int {
        return types.hashCode()
    }

    override fun toString(): String = types.joinToString(" | ")
}
