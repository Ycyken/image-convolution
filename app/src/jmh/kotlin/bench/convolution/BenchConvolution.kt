package bench.convolution

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import kernels.boxBlur
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit

enum class ConvolutionMode(val make: () -> ConvMode) {
    FullSeq({ ConvMode.Sequential(1u) }),
    ParallelChannels({ ConvMode.Sequential(3u) }),
    ParallelCols1({ ConvMode.ParallelCols(1) }),
    ParallelRows1({ ConvMode.ParallelRows(1) }),
    ParallelCols5({ ConvMode.ParallelCols(5) }),
    ParallelRows5({ ConvMode.ParallelRows(5) }),
    ParallelCols100({ ConvMode.ParallelCols(100) }),
    ParallelRows100({ ConvMode.ParallelRows(100) }),
    ParallelRect5x5({ ConvMode.ParallelRectangle(5, 5) }),
    ParallelRect50x50({ ConvMode.ParallelRectangle(50, 50) }),
    ParallelElems({ ConvMode.ParallelElems() }),
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(1)
open class BenchConvolution {
    @Param
    lateinit var mode: ConvolutionMode

    @Param("bird.png", "kha.bmp")
    lateinit var imageName: String
    private val kernel = boxBlur(13)
    private lateinit var image: BufferedImage
    private lateinit var convolution: Convolution

    @Setup(Level.Trial)
    fun setup() {
        convolution = Convolution(mode.make())
        val url = javaClass.classLoader.getResource("all_images/$imageName")
        image = UtilImageIO.loadImage(url)!!
    }

    @Benchmark
    fun convolutionBench() {
        val convolved = runBlocking { convolution.convolve(image, kernel) }
        return
    }
}

fun main() {
    val outFile = "build/results/jmh/convolution_results.json"
    File(outFile).parentFile.mkdirs()
    val opts =
        OptionsBuilder()
            .include(BenchConvolution::class.java.simpleName)
            .result(outFile)
            .resultFormat(ResultFormatType.JSON)
            .build()

    Runner(opts).run()
    println("Results written to $outFile")
}
