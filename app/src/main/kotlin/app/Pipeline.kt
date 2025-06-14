import boofcv.io.image.UtilImageIO
import convolution.Convolution
import convolution.Kernel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

data class NamedImage(val name: String, val img: BufferedImage)

fun imagesFlow(dir: File): Flow<NamedImage> = flow {
    dir.listFiles { x -> x.isFile }?.forEach { file ->
        println("read image: ${file.name}")
        val img = UtilImageIO.loadImage(file.absolutePath) ?: error("Can't load image: $file")
        emit(NamedImage(file.name, img))
    }
}.flowOn(Dispatchers.IO)

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<NamedImage>.convolve(
    convolution: Convolution,
    kernel: Kernel
): Flow<NamedImage> =
    this.flatMapMerge(concurrency = 4) { namedImg ->
        flow {
            println("convolve")
            val result = withContext(Dispatchers.Default) {
                convolution.convolve(namedImg.img, kernel)
            }
            emit(NamedImage(namedImg.name, result))
        }
    }

fun Flow<NamedImage>.saveTo(outputDir: File) =
    this.map { namedImg ->
        val out = File(outputDir, namedImg.name)
        ImageIO.write(namedImg.img, "png", out)
        println("Saved: ${out.name}")
    }.flowOn(Dispatchers.IO)

fun startPipeline(
    inputDir: File,
    convolution: Convolution,
    kernel: Kernel
) = runBlocking {
    val projectDir = File(System.getProperty("rootProjectDir"))
    val outputDir = File(projectDir, "output_images")
    if (!outputDir.exists()) outputDir.mkdirs()

    imagesFlow(inputDir)
        .buffer(10)
        .convolve(convolution, kernel)
        .saveTo(outputDir).collect()
}