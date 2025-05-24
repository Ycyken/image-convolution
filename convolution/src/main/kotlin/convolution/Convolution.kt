package convolution

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    val convolved =
        runBlocking {
            gray.convolve(kernel)
        }
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
    runBlocking {
        planar.bands.forEachIndexed { i, band ->
            launch(Dispatchers.Default) {
                convolved.setBand(
                    i,
                    band.convolve(kernel),
                )
            }
        }
    }

    return ConvertBufferedImage.convertTo_U8(convolved, null, true)
}

internal suspend fun GrayU8.convolve(kernel: Kernel): GrayU8 {
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

private fun <T : Number> convolvePoint(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
    x: Int,
    y: Int,
) {
    val kernelCenter = kernel.width / 2
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

private fun <T> validateConvolutionArgs(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
) {
    require(kernel.width % 2 == 1 && kernel.width == kernel.height) { "Kernel must be square matrix with odd size" }
    require(input.width == output.width && input.height == output.height) { "Input matrix must be same size as output matrix" }
}

private fun <T : Number> convolve(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) {
    validateConvolutionArgs(input, kernel, output)

    input.forEachIndexed { x, y, _ ->
        convolvePoint(input, kernel, output, transform, x, y)
    }
}

private suspend fun <T : Number> convolveParRows(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) = coroutineScope {
    validateConvolutionArgs(input, kernel, output)

    for (y in 0 until input.height) {
        launch(Dispatchers.Default) {
            for (x in 0 until input.width) {
                convolvePoint(input, kernel, output, transform, x, y)
            }
        }
    }
}

private suspend fun <T : Number> convolveParCols(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) = coroutineScope {
    validateConvolutionArgs(input, kernel, output)

    for (x in 0 until input.width) {
        launch(Dispatchers.Default) {
            for (y in 0 until input.height) {
                convolvePoint(input, kernel, output, transform, x, y)
            }
        }
    }
}

private suspend fun <T : Number> convolveParElements(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) = coroutineScope {
    validateConvolutionArgs(input, kernel, output)

    input.forEachIndexed { x, y, _ ->
        launch(Dispatchers.Default) {
            convolvePoint(input, kernel, output, transform, x, y)
        }
    }
}
