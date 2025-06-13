package bench

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import kernels.boxBlur
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.awt.image.BufferedImage
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
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
open class MyBenchmark {
    @Param
    lateinit var mode: ParallelMode
    private val kernel = boxBlur(21)
    private lateinit var image: BufferedImage
    private lateinit var convolution: Convolution

    @Setup(Level.Trial)
    fun setup() {
        val url = javaClass.classLoader.getResource("kha.bmp")
        image = UtilImageIO.loadImage(url)!!
        convolution = Convolution(mode.make())
    }

    @Benchmark
    fun convolutionBench() {
        val convolved = convolution.convolve(image, kernel)
        return
    }
}

fun main() {
    val opts =
        OptionsBuilder()
            .include(MyBenchmark::class.java.simpleName)
            .mode(Mode.AverageTime)
            .result("benchmark_results.csv")
            .resultFormat(ResultFormatType.CSV)
            .build()

    Runner(opts).run()
    println("Results written to benchmark_results.csv")
}
