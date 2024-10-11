package de.krall.spreadsheets.model

interface SparseGrid<T> {

    operator fun get(x: Int, y: Int): T?

    operator fun set(x: Int, y: Int, value: T?)

    val entries: Collection<Entry<T>>

    interface Entry<T> {
        val x: Int
        val y: Int

        val value: T
    }
}

fun <T> SparseGrid(): SparseGrid<T> {
    return MapBaseSparseGrid()
}

private class MapBaseSparseGrid<T> : SparseGrid<T> {

    private val map = mutableMapOf<Long, T>()

    override fun get(x: Int, y: Int): T? {
        return map[pack(x, y)]
    }

    override fun set(x: Int, y: Int, value: T?) {
        if (value != null) {
            map[pack(x, y)] = value
        } else {
            map.remove(pack(x, y))
        }
    }

    override val entries: Collection<SparseGrid.Entry<T>>
        get() = map.entries.map { (location, value) -> EntryImpl((location and 0xFFFFFFFF).toInt(), (location ushr 32).toInt(), value) }

    private fun pack(x: Int, y: Int): Long {
        return x.toLong() or (y.toLong() shl 32)
    }

    private fun unpack(location: Long): Pair<Int, Int> {
        return (location and 0xFFFFFFFF).toInt() to (location ushr 32).toInt()
    }

    private class EntryImpl<T>(
        override val x: Int,
        override val y: Int,
        override val value: T,
    ) : SparseGrid.Entry<T>
}