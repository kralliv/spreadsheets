package de.krall.spreadsheets.grid

fun <T> SparseGrid(): SparseGrid<T> {
    return MapBaseSparseGrid()
}

interface SparseGrid<T> {

    operator fun get(point: Point): T? = get(point.x, point.y)
    operator fun get(x: Int, y: Int): T?

    operator fun set(point: Point, value: T?) = set(point.x, point.y, value)
    operator fun set(x: Int, y: Int, value: T?)

    fun entries(area: Area): Collection<Entry<T>>

    val entries: Collection<Entry<T>>

    interface Entry<T> {
        val x: Int
        val y: Int

        val value: T
    }
}


private class MapBaseSparseGrid<T> : SparseGrid<T> {

    private val map = mutableMapOf<Long, EntryImpl<T>>()

    override fun get(x: Int, y: Int): T? {
        return map[pack(x, y)]?.value
    }

    override fun set(x: Int, y: Int, value: T?) {
        if (value != null) {
            map[pack(x, y)] = EntryImpl(x, y, value)
        } else {
            map.remove(pack(x, y))
        }
    }

    override fun entries(area: Area): Collection<SparseGrid.Entry<T>> {
        if (area is FiniteArea && area.size < map.size) {
            area.points.map { get(it.x, it.y) }
        }

        return map.values.filter { area.contains(it.x, it.y) }
    }

    override val entries: Collection<SparseGrid.Entry<T>>
        get() = map.values

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