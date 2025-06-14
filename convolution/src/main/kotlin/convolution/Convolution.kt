package convolution

import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import kotlinx.coroutines.*
import java.awt.image.BufferedImage
import kotlin.math.min
import kotlin.math.roundToInt

class Convolution(private val mode: ConvMode) {
    private val dispatcher: CoroutineDispatcher =
        if (mode.thrs == 0u) Dispatchers.Default else Dispatchers.Default.limitedParallelism(mode.thrs.toInt())

    suspend fun convolve(
        input: BufferedImage,
        kernel: Kernel,
    ): BufferedImage = coroutineScope {
        when (detectImageType(input)) {
            ImageType.GRAY -> convolveGray(input, kernel)
            ImageType.RGB -> convolveRGB(input, kernel)
            ImageType.UNKNOWN -> throw IllegalArgumentException("Unsupported image type: ${input.colorModel}")
        }
    }

    private suspend fun convolveGray(
        image: BufferedImage,
        kernel: Kernel,
    ): BufferedImage = coroutineScope {
        val gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8::class.java)
        val convolved = convolveBand(gray, kernel)
        ConvertBufferedImage.convertTo(convolved, null)
    }

    private suspend fun convolveRGB(
        image: BufferedImage,
        kernel: Kernel,
    ): BufferedImage = coroutineScope {
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
                launch(dispatcher) {
                    convolved.setBand(
                        i,
                        convolveBand(band, kernel),
                    )
                }
            }
        }

        ConvertBufferedImage.convertTo_U8(convolved, null, true)
    }

    internal suspend fun convolveBand(
        gray: GrayU8,
        kernel: Kernel,
    ): GrayU8 = coroutineScope {
        val input = MatrixAdapter(gray)
        val output = MatrixAdapter(gray.createSameShape())
        val transform = { x: Float -> x.roundToInt().coerceIn(0, 255) }
        when (mode) {
            is ConvMode.Sequential -> convolveSeq(input, kernel, output, transform)
            is ConvMode.ParallelRows ->
                convolveParRows(
                    input,
                    kernel,
                    output,
                    transform,
                    mode,
                )

            is ConvMode.ParallelCols ->
                convolveParCols(
                    input,
                    kernel,
                    output,
                    transform,
                    mode,
                )

            is ConvMode.ParallelRectangle ->
                convolveParRects(
                    input,
                    kernel,
                    output,
                    transform,
                    mode,
                )

            is ConvMode.ParallelElems -> convolveParElements(input, kernel, output, transform)
        }
        output.gray
    }

    private suspend fun <T : Number> convolveParRows(
        input: ReadableMatrix<T>,
        kernel: Kernel,
        output: WritableMatrix<T>,
        transform: (Float) -> T,
        mode: ConvMode.ParallelRows,
    ) = coroutineScope {
        validateConvolutionArgs(input, output)

        val batchSize = mode.batchSize
        val jobsNumber = (input.height + batchSize - 1) / batchSize

        for (job in 0 until jobsNumber) {
            val startY = job * batchSize
            val endY = min(startY + batchSize, input.height)
            launch(dispatcher) {
                for (y in startY until endY) {
                    for (x in 0 until input.width) {
                        convolvePoint(input, kernel, output, transform, x, y)
                    }
                }
            }
        }
    }

    private suspend fun <T : Number> convolveParCols(
        input: ReadableMatrix<T>,
        kernel: Kernel,
        output: WritableMatrix<T>,
        transform: (Float) -> T,
        mode: ConvMode.ParallelCols,
    ) = coroutineScope {
        validateConvolutionArgs(input, output)

        val batchSize = mode.batchSize
        val jobsNumber = (input.width + batchSize - 1) / batchSize

        for (job in 0 until jobsNumber) {
            val startX = job * batchSize
            val endX = min(startX + batchSize, input.width)
            launch(dispatcher) {
                for (x in startX until endX) {
                    for (y in 0 until input.height) {
                        convolvePoint(input, kernel, output, transform, x, y)
                    }
                }
            }
        }
    }

    private suspend fun <T : Number> convolveParRects(
        input: ReadableMatrix<T>,
        kernel: Kernel,
        output: WritableMatrix<T>,
        transform: (Float) -> T,
        mode: ConvMode.ParallelRectangle,
    ) = coroutineScope {
        validateConvolutionArgs(input, output)

        val numRectsX = (input.width + mode.width - 1) / mode.width
        val numRectsY = (input.height + mode.height - 1) / mode.height

        for (rectY in 0 until numRectsY) {
            for (rectX in 0 until numRectsX) {
                val startX = rectX * mode.width
                val endX = min(startX + mode.width, input.width)
                val startY = rectY * mode.height
                val endY = min(startY + mode.height, input.height)
                launch(dispatcher) {
                    for (y in startY until endY) {
                        for (x in startX until endX) {
                            convolvePoint(input, kernel, output, transform, x, y)
                        }
                    }
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
        validateConvolutionArgs(input, output)

        input.forEachIndexed { x, y, _ ->
            launch(dispatcher) {
                convolvePoint(input, kernel, output, transform, x, y)
            }
        }
    }
}
