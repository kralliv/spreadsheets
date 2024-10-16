package de.krall.spreadsheets.grid

fun <T> SparseGrid(): SparseGrid<T> {
    return MapBaseSparseGrid()
}

interface SparseGrid<T> {

    operator fun get(point: Point): T? = get(point.x, point.y)
    operator fun get(x: Int, y: Int): T?

    operator fun set(point: Point, value: T?) = set(point.x, point.y, value)
    operator fun set(x: Int, y: Int, value: T?)

    fun entries(area: Area): Sequence<Entry<T>>

    val entries: Sequence<Entry<T>>

    interface Entry<T> {
        val x: Int
        val y: Int

        val value: T
    }
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

    override fun entries(area: Area): Sequence<SparseGrid.Entry<T>> {
        if (area is FiniteArea && area.size < map.size) {
            return sequence {
                for (point in area.points) {
                    val value = get(point.x, point.y) ?: continue
                    yield(EntryImpl(point.x, point.y, value))
                }
            }
        }

        return sequence {
            for ((location, value) in map) {
                val x = (location and 0xFFFFFFFF).toInt()
                val y = (location ushr 32).toInt()
                if (!area.contains(x, y)) continue
                yield(EntryImpl(x, y, value))
            }
        }
    }

    override val entries: Sequence<SparseGrid.Entry<T>>
        get() = map.asSequence().map { (location, value) ->
            val x = (location and 0xFFFFFFFF).toInt()
            val y = (location ushr 32).toInt()
            EntryImpl(x, y, value)
        }

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