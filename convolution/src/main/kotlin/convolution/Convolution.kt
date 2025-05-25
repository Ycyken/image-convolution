package convolution

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import kotlin.math.min
import kotlin.math.roundToInt

enum class ImageType {
    GRAY,
    RGB,
    UNKNOWN,
}

sealed class ConvMode {
    data object Sequential : ConvMode()

    data class ParallelRows(val batchSize: Int) : ConvMode()

    data class ParallelCols(val batchSize: Int) : ConvMode()

    data class ParallelRectangle(val width: Int, val height: Int) : ConvMode()

    data object ParallelElems : ConvMode()
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
    mode: ConvMode,
): BufferedImage {
    return when (detectImageType(input)) {
        ImageType.GRAY -> convolveGray(input, kernel, mode)
        ImageType.RGB -> convolveRGB(input, kernel, mode)
        ImageType.UNKNOWN -> throw IllegalArgumentException("Unsupported image type: ${input.colorModel}")
    }
}

private fun convolveGray(
    image: BufferedImage,
    kernel: Kernel,
    mode: ConvMode,
): BufferedImage {
    val gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8::class.java)
    val convolved = gray.convolve(kernel, mode)
    return ConvertBufferedImage.convertTo(convolved, null)
}

private fun convolveRGB(
    image: BufferedImage,
    kernel: Kernel,
    mode: ConvMode,
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
                    band.convolve(kernel, mode),
                )
            }
        }
    }

    return ConvertBufferedImage.convertTo_U8(convolved, null, true)
}

internal fun GrayU8.convolve(
    kernel: Kernel,
    mode: ConvMode,
): GrayU8 {
    val input = MatrixAdapter(this)
    val output = MatrixAdapter(this.createSameShape())
    val transform = { x: Float -> x.roundToInt().coerceIn(0, 255) }
    when (mode) {
        is ConvMode.Sequential -> convolveSeq(input, kernel, output, transform)
        is ConvMode.ParallelRows ->
            convolveParRows(
                input,
                kernel,
                output,
                transform,
                mode.batchSize,
            )

        is ConvMode.ParallelCols ->
            convolveParCols(
                input,
                kernel,
                output,
                transform,
                mode.batchSize,
            )

        is ConvMode.ParallelRectangle -> throw IllegalArgumentException("Rectangle convolution mode is not supported yet")
        is ConvMode.ParallelElems -> convolveParElements(input, kernel, output, transform)
    }
    return output.gray
}

internal fun Kernel.convolve(other: Kernel): Kernel {
    val output = Kernel(this.width)
    val transform = { x: Float -> x.coerceIn(0.0F, 255.0F) }
    convolveSeq(this, other, output, transform)
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
        val currX = x + (kernelX - kernelCenter)
        val currY = y + (kernelY - kernelCenter)
        val correspondingValue =
            if (currX in 0 until input.width && currY in 0 until input.height) {
                usedWeights += kernelValue
                input.unsafeGet(currX, currY).toFloat()
            } else {
                0.0F
            }
        convolvedValue += correspondingValue * kernelValue
    }

    if (usedWeights != 0.0F) {
        convolvedValue /= usedWeights
    }
    val transformedValue = transform(convolvedValue)
    output.unsafeSet(x, y, transformedValue)
}

private fun <T> validateConvolutionArgs(
    input: ReadableMatrix<T>,
    output: WritableMatrix<T>,
) {
    require(input.width == output.width && input.height == output.height) { "Input matrix must be same size as output matrix" }
}

private fun <T : Number> convolveSeq(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) {
    validateConvolutionArgs(input, output)

    input.forEachIndexed { x, y, _ ->
        convolvePoint(input, kernel, output, transform, x, y)
    }
}

private fun <T : Number> convolveParRows(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
    batchSize: Int,
) = runBlocking {
    validateConvolutionArgs(input, output)

    val jobsNumber = (input.height + batchSize - 1) / batchSize
    for (job in 0 until jobsNumber) {
        val startY = job * batchSize
        val endY = min(startY + batchSize, input.height)
        launch(Dispatchers.Default) {
            for (y in startY until endY) {
                for (x in 0 until input.width) {
                    convolvePoint(input, kernel, output, transform, x, y)
                }
            }
        }
    }
}

private fun <T : Number> convolveParCols(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
    batchSize: Int,
) = runBlocking {
    validateConvolutionArgs(input, output)

    val jobsNumber = (input.width + batchSize - 1) / batchSize
    for (job in 0 until jobsNumber) {
        val startX = job * batchSize
        val endX = min(startX + batchSize, input.width)
        launch(Dispatchers.Default) {
            for (x in startX until endX) {
                for (y in 0 until input.height) {
                    convolvePoint(input, kernel, output, transform, x, y)
                }
            }
        }
    }
}

private fun <T : Number> convolveParElements(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) = runBlocking {
    validateConvolutionArgs(input, output)

    input.forEachIndexed { x, y, _ ->
        launch(Dispatchers.Default) {
            convolvePoint(input, kernel, output, transform, x, y)
        }
    }
}
