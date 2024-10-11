package de.krall.spreadsheets.util

fun <E> MutableCollection<E>.empty(): List<E> {
    val copy = toMutableList()
    clear()
    return copy
}
