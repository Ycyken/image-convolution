package bench

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.convolve
import kernels.boxBlur
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit

enum class ParallelMode(val make: () -> ConvMode) {
    ParallelSeq({ ConvMode.Sequential }),
    ParallelCols1({ ConvMode.ParallelCols(1) }),
    ParallelCols5({ ConvMode.ParallelCols(5) }),
    ParallelCols20({ ConvMode.ParallelCols(20) }),
    ParallelCols300({ ConvMode.ParallelCols(300) }),
    ParallelRows1({ ConvMode.ParallelRows(1) }),
    ParallelRows5({ ConvMode.ParallelRows(5) }),
    ParallelRows20({ ConvMode.ParallelRows(20) }),
    ParallelRows300({ ConvMode.ParallelRows(300) }),
    ParallelRect5x5({ ConvMode.ParallelRectangle(5, 5) }),
    ParallelRect50x5({ ConvMode.ParallelRectangle(50, 5) }),
    ParallelRect5x50({ ConvMode.ParallelRectangle(5, 50) }),
    ParallelRect100x100({ ConvMode.ParallelRectangle(100, 100) }),
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(4)
open class MyBenchmark {
    @Param
    lateinit var mode: ParallelMode
    private val kernel = boxBlur(21)
    private lateinit var image: BufferedImage

    @Setup(Level.Trial)
    fun setup() {
        val url = javaClass.classLoader.getResource("bird.png")
        image = UtilImageIO.loadImage(url)!!
    }

    @Benchmark
    fun convolutionBench() {
        val convolved = convolve(image, kernel, mode.make())
        return
    }
}

fun main() {
    val opts =
        OptionsBuilder()
            .include(MyBenchmark::class.java.simpleName)
            .mode(Mode.AverageTime)
            .warmupIterations(10)
            .measurementIterations(40)
            .result("benchmark_results.csv")
            .resultFormat(ResultFormatType.CSV)
            .build()

    Runner(opts).run()
    println("Results written to benchmark_results.csv")
}
