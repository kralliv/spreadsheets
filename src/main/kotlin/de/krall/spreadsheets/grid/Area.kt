package de.krall.spreadsheets.grid


interface Area {
    fun contains(x: Int, y: Int): Boolean
}

interface FiniteArea : Area {

    val size: Long

    val points: Sequence<Point>
}

data class Cell(
    val x: Int,
    val y: Int,
) : FiniteArea {

    override val size: Long
        get() = 1

    override val points: Sequence<Point>
        get() = sequenceOf(Point(x, y))

    override fun contains(x: Int, y: Int): Boolean {
        return x == this.x && y == this.y
    }
}

data class Rectangle(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) : FiniteArea {

    init {
        require(width > 0) { "width must be positive" }
        require(width > 0) { "height must be positive" }
    }

    override val size: Long
        get() = width.toLong() * height.toLong()

    override val points: Sequence<Point>
        get() = sequence {
            for (ox in 0..<width) {
                for (oy in 0..<height) {
                    yield(Point(x + ox, y + oy))
                }
            }
        }

    override fun contains(x: Int, y: Int): Boolean {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height
    }
}

interface InfiniteArea : Area

data class Column(val x: Int, val width: Int, val y: Int? = null) : InfiniteArea {

    override fun contains(x: Int, y: Int): Boolean {
        return x >= this.x && x < this.x + width && (this.y == null || y >= this.y)
    }
}

data class Row(val y: Int, val height: Int, val x: Int? = null) : InfiniteArea {

    override fun contains(x: Int, y: Int): Boolean {
        return y >= this.y && y < this.y + height && (this.x == null || x >= this.x)
    }
}
