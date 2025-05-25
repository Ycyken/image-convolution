package convolution

import boofcv.io.image.UtilImageIO
import kernels.boxBlur
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ParallelConvolution {
    companion object {
        @JvmStatic
        fun convolutionModes(): List<ConvMode> =
            listOf(
                ConvMode.ParallelRows(1),
                ConvMode.ParallelRows(10),
                ConvMode.ParallelRows(1000),
                ConvMode.ParallelCols(1),
                ConvMode.ParallelCols(10),
                ConvMode.ParallelCols(1000),
                ConvMode.ParallelElems,
            )
    }

    @ParameterizedTest
    @MethodSource("convolutionModes")
    fun `parallel convolution with box blur is the same as sequential`(convolutionMode: ConvMode) {
        val url = javaClass.classLoader.getResource("bird.png")
        val image = UtilImageIO.loadImage(url)!!

        val kernel = boxBlur(21)
        val convolvedPar = convolve(image, kernel, convolutionMode)
        val convolvedSeq = convolve(image, kernel, ConvMode.Sequential)

        val actual = IntArray(convolvedPar.width * convolvedPar.height)
        val expected = IntArray(convolvedSeq.width * convolvedSeq.height)
        convolvedPar.getRGB(
            0,
            0,
            convolvedPar.width,
            convolvedPar.height,
            actual,
            0,
            convolvedPar.width,
        )
        convolvedSeq.getRGB(
            0,
            0,
            convolvedSeq.width,
            convolvedSeq.height,
            expected,
            0,
            convolvedSeq.width,
        )
        assertArrayEquals(expected, actual)
    }
}
