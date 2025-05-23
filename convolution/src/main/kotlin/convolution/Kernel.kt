package convolution

class Kernel private constructor(
    val matrix: Array<FloatArray>,
    override val width: Int,
    override val height: Int,
) : Matrix<Float> {
    init {
        require(matrix.size == height) { "The matrix must be of the specified size" }
        require(matrix.all { it.size == width }) { "The matrix must be of the specified size" }
    }

    constructor(matrix: Array<FloatArray>) : this(
        matrix,
        matrix.firstOrNull()?.size ?: 0,
        matrix.size,
    )

    constructor(width: Int, height: Int) : this(
        Array(height) { FloatArray(width) },
        width,
        height,
    )

    override operator fun get(
        x: Int,
        y: Int,
    ): Float {
        require(x in 0 until this.width && y in 0 until this.height) {
            "Invalid x or y in get($x, $y): there is ${this.width} width and ${this.height} height"
        }
        return matrix[y][x]
    }

    override operator fun set(
        x: Int,
        y: Int,
        value: Float,
    ) {
        require(x in 0 until this.width && y in 0 until this.height) {
            "Invalid x or y in set($x, $y): there is ${this.width} width and ${this.height} height"
        }
        matrix[y][x] = value
    }

    override fun forEachIndexed(action: (x: Int, y: Int, value: Float) -> Unit) {
        matrix.forEachIndexed { y, row ->
            row.forEachIndexed { x, value ->
                action(
                    x,
                    y,
                    value,
                )
            }
        }
    }
}
