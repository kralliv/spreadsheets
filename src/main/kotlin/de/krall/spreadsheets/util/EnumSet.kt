package de.krall.spreadsheets.util

import java.util.EnumSet

inline fun <reified E : Enum<E>> Iterable<E>.toEnumSet(): EnumSet<E> {
    val set = EnumSet.noneOf(E::class.java)
    set.addAll(this)
    return set
}

operator fun <E : Enum<E>> EnumSet<E>.plus(other: EnumSet<E>): EnumSet<E> {
    val set = EnumSet.copyOf(this)
    set.addAll(other)
    return set
}
