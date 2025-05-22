package convolution

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

enum class ImageType {
    GRAY, RGB, UNKNOWN
}

fun detectImageType(image: BufferedImage): ImageType {
    val numComponents = image.colorModel.numComponents
    val bits = image.sampleModel.getSampleSize(0)

    return when {
        numComponents == 1 && bits == 8 -> ImageType.GRAY
        numComponents == 3 && bits == 8 -> ImageType.RGB
        else -> ImageType.UNKNOWN
    }
}

fun convolve(
    input: BufferedImage,
    kernel: FloatMatrix
): BufferedImage {
    return when (detectImageType(input)) {
        ImageType.GRAY -> convolveGray(input, kernel)
        ImageType.RGB -> convolveRGB(input, kernel)
        ImageType.UNKNOWN -> throw IllegalArgumentException("Unsupported image type: ${input.colorModel}")
    }
}

private fun convolveGray(
    image: BufferedImage,
    kernel: FloatMatrix
): BufferedImage {
    val gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8::class.java)
    val matrix = gray.toMatrix()
    val convolvedMatrix = matrix.convolve(kernel)
    val grayConvolved = GrayU8(convolvedMatrix.matrix)
    return ConvertBufferedImage.convertTo(grayConvolved, null)
}

private fun convolveRGB(
    image: BufferedImage,
    kernel: FloatMatrix
): BufferedImage {
    val planar = ConvertBufferedImage.convertFromPlanar(
        image,
        null,
        true,
        GrayU8::class.java
    )
    val matrices = planar.toMatrices()
    val convolvedMatrices = matrices.map { it.convolve(kernel) }
    val convolvedGrays = convolvedMatrices.map { GrayU8(it.matrix) }

    val convolvedPlanar = planar.createSameShape()
    convolvedGrays.forEachIndexed { i, mat -> convolvedPlanar.setBand(i, mat) }
    return ConvertBufferedImage.convertTo_U8(convolvedPlanar, null, true)
}

fun Matrix<Byte>.convolve(kernel: FloatMatrix): ByteMatrix {
    if (kernel.rows % 2 == 0 || kernel.rows != kernel.cols) {
        throw IllegalArgumentException("Kernel must be square matrix with odd size")
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
    return ByteMatrix(convolved)
}
