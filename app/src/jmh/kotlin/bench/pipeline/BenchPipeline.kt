package bench.pipeline

import app.startSeqPipeline
import convolution.ConvMode
import kernels.boxBlur
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import startAsyncPipeline
import java.io.File
import java.util.concurrent.TimeUnit

enum class ConvolutionMode(val make: () -> ConvMode) {
    FullSeq({ ConvMode.Sequential(1u) }),
    ParallelChannels({ ConvMode.Sequential(3u) }),
    ParallelCols5({ ConvMode.ParallelCols(5) }),
    ParallelRows5({ ConvMode.ParallelRows(5) }),
    ParallelRect50x50({ ConvMode.ParallelRectangle(50, 50) }),
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 20)
@Fork(1)
open class BenchPipeline {
    @Param
    lateinit var mode: ConvolutionMode

    @Param("images_links")
    lateinit var dirName: String
    private val kernel = boxBlur(13)
    private lateinit var inputDir: File

    @Setup(Level.Trial)
    fun setup() {
        val projectDir = File(System.getProperty("rootProjectDir"))
        inputDir = File(projectDir, "images/$dirName")
    }

    @Benchmark
    fun parallelPipeline() {
        startAsyncPipeline(inputDir, mode.make(), kernel)
    }

    @Benchmark
    fun seqPipeline() {
        startSeqPipeline(inputDir, mode.make(), kernel)
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
