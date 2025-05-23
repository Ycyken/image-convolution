package convolution

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

enum class ImageType {
    GRAY,
    RGB,
    UNKNOWN,
}

private fun detectImageType(image: BufferedImage): ImageType {
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
    kernel: Kernel,
): BufferedImage {
    return when (detectImageType(input)) {
        ImageType.GRAY -> convolveGray(input, kernel)
        ImageType.RGB -> convolveRGB(input, kernel)
        ImageType.UNKNOWN -> throw IllegalArgumentException("Unsupported image type: ${input.colorModel}")
    }
}

private fun convolveGray(
    image: BufferedImage,
    kernel: Kernel,
): BufferedImage {
    val gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8::class.java)
    val convolved = gray.convolve(kernel)
    return ConvertBufferedImage.convertTo(convolved, null)
}

private fun convolveRGB(
    image: BufferedImage,
    kernel: Kernel,
): BufferedImage {
    val planar =
        ConvertBufferedImage.convertFromPlanar(
            image,
            null,
            true,
            GrayU8::class.java,
        )
    val convolved = planar.createSameShape()
    planar.bands.forEachIndexed { i, band -> convolved.setBand(i, band.convolve(kernel)) }
    return ConvertBufferedImage.convertTo_U8(convolved, null, true)
}

private fun <T : Number> convolve(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) {
    require(kernel.width % 2 == 1 && kernel.width == kernel.height) { "Kernel must be square matrix with odd size" }
    require(input.width == output.width && input.height == output.height) { "Input matrix must be same size as output matrix" }

    val kernelCenter = kernel.width / 2
    input.forEachIndexed { x, y, inputValue ->
        var convolvedValue = 0.0F
        var usedWeights = 0.0F
        kernel.forEachIndexed { kernelX, kernelY, kernelValue ->
            try {
                val correspondingValue =
                    input[
                        x + (kernelX - kernelCenter),
                        y + (kernelY - kernelCenter),
                    ].toFloat()
                convolvedValue += correspondingValue * kernelValue
                usedWeights += kernelValue
            } catch (_: Exception) {
            }
        }

        if (usedWeights != 0.0F) {
            convolvedValue /= usedWeights
        }
        val transformedValue = transform(convolvedValue)
        output.unsafeSet(x, y, transformedValue)
    }
}

internal fun GrayU8.convolve(kernel: Kernel): GrayU8 {
    val input = MatrixAdapter(this)
    val output = MatrixAdapter(this.createSameShape())
    val transform = { x: Float -> x.roundToInt().coerceIn(0, 255) }
    convolve(input, kernel, output, transform)
    return output.gray
}

internal fun Kernel.convolve(other: Kernel): Kernel {
    val output = Kernel(this.width, this.height)
    val transform = { x: Float -> x.coerceIn(0.0F, 255.0F) }
    convolve(this, other, output, transform)
    return output
}
