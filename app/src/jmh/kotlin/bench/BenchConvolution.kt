package bench

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

enum class ParallelMode(val make: () -> ConvMode) {
    ParallelFullSeq({ ConvMode.Sequential(1u) }),
    ParallelConSeq({ ConvMode.Sequential(3u) }),
    ParallelCols5({ ConvMode.ParallelCols(5) }),
    ParallelRows1({ ConvMode.ParallelRows(5) }),
    ParallelRect5x5({ ConvMode.ParallelRectangle(50, 50) }),
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 2)
@Fork(1)
open class BenchConvolution {
    @Param
    lateinit var mode: ParallelMode

    @Param("bird.png", "kha.bmp")
    lateinit var imageName: String
    private val kernel = boxBlur(13)
    private lateinit var image: BufferedImage
    private lateinit var convolution: Convolution

    @Setup(Level.Trial)
    fun setup() {
        val url = javaClass.classLoader.getResource("all_images/${imageName}")
        image = UtilImageIO.loadImage(url)!!
        convolution = Convolution(mode.make())
    }

    @Benchmark
    fun convolutionBench() {
        val convolved = runBlocking { convolution.convolve(image, kernel) }
        return
    }
}

fun main() {
    val outFile = "build/results/jmh/convolution_results.csv"
    File(outFile).parentFile.mkdirs()
    val opts =
        OptionsBuilder()
            .include(BenchConvolution::class.java.simpleName)
            .result(outFile)
            .resultFormat(ResultFormatType.CSV)
            .build()

    Runner(opts).run()
    println("Results written to $outFile")
}
