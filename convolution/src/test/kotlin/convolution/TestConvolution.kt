package convolution

import kernels.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TestConvolution {
    companion object {
        @JvmStatic
        fun imageAndMode(): Stream<Arguments> {
            val images = listOf("dog.jpg", "kha.bmp", "small.bmp")
            val modes =
                listOf(
                    ConvMode.Sequential(),
                    ConvMode.ParallelRows(10),
                    ConvMode.ParallelCols(10),
                    ConvMode.ParallelRectangle(30, 30),
                    ConvMode.ParallelElems(),
                )
            return images.flatMap { image ->
                modes.map { mode -> Arguments.of(image, mode) }
            }.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with id filter is correct`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val kernel = id(19)
        val convolution = Convolution(ConvMode.Sequential())
        assertConvolutions(image, kernel, convolution, ::convolveBoofcv)
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with boxBlur is correct`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val kernel = boxBlur(19)
        val convolution = Convolution(mode)
        assertConvolutions(image, kernel, convolution, ::convolveBoofcv)
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with motionBlur is correct`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val kernel = motionBlur(15)
        val convolution = Convolution(mode)
        assertConvolutions(image, kernel, convolution, ::convolveBoofcv)
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with gaussian 3x3 filter is correct`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val kernel = gaussianBlur3x3()
        val convolution = Convolution(mode)
        assertConvolutions(image, kernel, convolution, ::convolveBoofcv)
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with gaussian 5x5 filter is correct`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val kernel = gaussianBlur5x5()
        val convolution = Convolution(mode)
        assertConvolutions(image, kernel, convolution, ::convolveBoofcv)
    }

    @ParameterizedTest
    @MethodSource("imageAndMode")
    fun `convolution with extra zeros in boxBlur is the same`(
        imageName: String,
        mode: ConvMode,
    ) {
        val image = getImgResource(imageName)

        val size = 9
        val realSize = 5
        val normalizer = 1.0F / (realSize * realSize).toFloat()
        val boxZeros =
            Kernel(Array(size) { y -> FloatArray(size) { x -> if (x in 2..6 && y in 2..6) 1.0F else (0.0F) } }) * normalizer
        val box = boxBlur(realSize)

        val convolution = Convolution(mode)
        val convolvedBoxZeros = runBlocking { convolution.convolve(image, boxZeros) }
        val convolvedBox = runBlocking { convolution.convolve(image, box) }

        assertImgEquals(convolvedBox, convolvedBoxZeros)
    }
}
