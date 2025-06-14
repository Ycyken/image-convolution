import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import convolution.Kernel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

data class NamedImage(val name: String, val img: BufferedImage)

fun imagesFlow(dir: File): Flow<NamedImage> =
    flow {
        dir.listFiles { f -> Files.isRegularFile(f.toPath()) }?.forEach { file ->
            println("Start read image ${file.name}")
            val img = UtilImageIO.loadImage(file.absolutePath) ?: error("Can't load image: $file")
            emit(NamedImage(file.name, img))
            println("Successfully read image ${file.name}")
        }
    }.flowOn(Dispatchers.IO)

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<NamedImage>.convolve(
    mode: ConvMode,
    kernel: Kernel,
): Flow<NamedImage> =
    this.flatMapMerge(8) { namedImg ->
        flow {
            println("Start convolve image ${namedImg.name}")
            val convolution = Convolution(mode)
            val result =
                withContext(Dispatchers.Default) {
                    convolution.convolve(namedImg.img, kernel)
                }
            emit(NamedImage(namedImg.name, result))
            println("Successfully convolved image ${namedImg.name}")
        }
    }

fun Flow<NamedImage>.saveTo(outputDir: File) =
    this.map { namedImg ->
        println("Start save image: ${namedImg.name}")
        val out = File(outputDir, namedImg.name)
        ImageIO.write(namedImg.img, "png", out)
        println("Successfully saved image ${namedImg.name}")
    }.flowOn(Dispatchers.IO)

fun startAsyncPipeline(
    inputDir: File,
    mode: ConvMode,
    kernel: Kernel,
) = runBlocking {
    val projectDir = File(System.getProperty("rootProjectDir"))
    val outputDir = File(projectDir, "output_images")
    if (!outputDir.exists()) outputDir.mkdirs()

    imagesFlow(inputDir)
        .buffer(10)
        .convolve(mode, kernel)
        .saveTo(outputDir).collect()
}
