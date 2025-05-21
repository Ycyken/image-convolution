package convolution

import boofcv.struct.image.GrayU8

interface Matrix<T> {
    val rows: Int
    val cols: Int

    operator fun get(
        row: Int,
        col: Int,
    ): Result<T>

    operator fun set(
        row: Int,
        col: Int,
        value: T,
    ): Result<Unit>

    fun getOrDefault(
        row: Int,
        col: Int,
        default: T,
    ): T {
        if (row !in 0 until this.rows || col !in 0 until this.cols) {
            return default
        }
        return get(row, col).getOrThrow()
    }

    fun setOrIgnore(
        row: Int,
        col: Int,
        value: T,
    ) {
        if (row in 0 until this.rows && col in 0 until this.cols) {
            set(row, col, value)
        }
    }

    fun forEachIndexed(action: (row: Int, col: Int, value: T) -> Unit)
}

fun GrayU8.toMatrix(): ByteMatrix {
    val byteMatrix = Array(this.height) { y -> ByteArray(this.width) { x -> this.get(x, y).toByte() } }
    return ByteMatrix(byteMatrix)
}

/**
 * Can't use Generics for Matrix classes since ByteArray and FloatArray are not generalized
 * and differs from Array<Byte> and Array<Float> by performance.
 */
class ByteMatrix private constructor(
    val matrix: Array<ByteArray>,
    override val rows: Int,
    override val cols: Int,
) : Matrix<Byte> {
    constructor(matrix: Array<ByteArray>) : this(
        matrix,
        matrix.size,
        matrix.firstOrNull()?.size ?: 0,
    ) {
        require(matrix.isNotEmpty()) { "Matrix must be not empty" }
        val cols = matrix[0].size
        require(matrix.all { it.size == cols }) { "Matrix must be rectangle form" }
    }

    constructor(rows: Int, cols: Int) : this(
        Array(rows) { ByteArray(cols) },
        rows,
        cols,
    )

    override fun get(
        row: Int,
        col: Int,
    ): Result<Byte> {
        if (row !in 0 until this.rows || col !in 0 until this.cols) {
            return Result.failure(IndexOutOfBoundsException("Invalid row or column:  $row, $col"))
        }
        return Result.success(matrix[row][col])
    }

    override fun set(
        row: Int,
        col: Int,
        value: Byte,
    ): Result<Unit> {
        if (row !in 0 until this.rows || col !in 0 until this.cols) {
            return Result.failure(IndexOutOfBoundsException("Invalid row or column: $row, $col"))
        }
        matrix[row][col] = value
        return Result.success(Unit)
    }

    override fun forEachIndexed(action: (row: Int, col: Int, value: Byte) -> Unit) {
        matrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, byte ->
                action(
                    rowIndex,
                    colIndex,
                    byte,
                )
            }
        }
    }
}

class FloatMatrix private constructor(
    private val matrix: Array<FloatArray>,
    override val rows: Int,
    override val cols: Int,
) : Matrix<Float> {
    constructor(matrix: Array<FloatArray>) : this(
        matrix,
        matrix.size,
        matrix.firstOrNull()?.size ?: 0,
    ) {
        require(matrix.isNotEmpty()) { "Matrix must be not empty" }
        val cols = matrix[0].size
        require(matrix.all { it.size == cols }) { "Matrix must be rectangle form" }
    }

    constructor(rows: Int, cols: Int) : this(
        Array(rows) { FloatArray(cols) },
        rows,
        cols,
    )

    override fun get(
        row: Int,
        col: Int,
    ): Result<Float> {
        if (row !in 0 until this.rows || col !in 0 until this.cols) {
            return Result.failure(IndexOutOfBoundsException("Invalid row or column:  $row, $col"))
        }
        return Result.success(matrix[row][col])
    }

    override fun set(
        row: Int,
        col: Int,
        value: Float,
    ): Result<Unit> {
        if (row !in 0 until this.rows || col !in 0 until this.cols) {
            return Result.failure(IndexOutOfBoundsException("Invalid row or column: $row, $col"))
        }
        matrix[row][col] = value
        return Result.success(Unit)
    }

    override fun forEachIndexed(action: (row: Int, col: Int, value: Float) -> Unit) {
        matrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, byte ->
                action(
                    rowIndex,
                    colIndex,
                    byte,
                )
            }
        }
    }
}
