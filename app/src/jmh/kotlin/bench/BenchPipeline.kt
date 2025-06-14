package bench

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import kernels.boxBlur
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import startPipeline
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
open class BenchPipeline {
    @Param("small_images_pack", "big_images_pack")
    lateinit var dirName: String
    private val kernel = boxBlur(13)
    private val convolution = Convolution(ConvMode.ParallelRows(5))
    private lateinit var inputDir: File

    @Setup(Level.Trial)
    fun setup() {
        val projectDir = File(System.getProperty("rootProjectDir"))
        inputDir = File(projectDir, "images/$dirName")
    }

    @Benchmark
    fun convolutionBench() {
        startPipeline(inputDir, convolution, kernel)
        return
    }
}

fun main() {
    val outFile = "build/results/jmh/pipeline_results.csv"
    File(outFile).parentFile.mkdirs()
    val opts =
        OptionsBuilder()
            .include(BenchPipeline::class.java.simpleName)
            .result(outFile)
            .resultFormat(ResultFormatType.CSV)
            .build()

    Runner(opts).run()
    println("Results written to $outFile")
}
