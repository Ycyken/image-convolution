package convolution

import kotlin.math.roundToInt

fun Matrix<Byte>.convolve(kernel: FloatMatrix): Result<ByteMatrix> {
    if (kernel.rows % 2 == 0 || kernel.rows != kernel.cols) {
        return Result.failure(IllegalArgumentException("kernel must be square matrix with odd size"))
    }
    val kernelCenter = kernel.rows / 2

    val convolved =
        Array(this.rows) { row ->
            ByteArray(this.cols) { col ->
                var convolvedValue = 0.0
                kernel.forEachIndexed { rowK, colK, valueK ->
                    convolvedValue += valueK *
                        this.getOrDefault(
                            row + (rowK - kernelCenter),
                            col + (colK - kernelCenter),
                            0,
                        )
                }
                convolvedValue.roundToInt().toByte()
            }
        }
    return Result.success(ByteMatrix(convolved))
}
