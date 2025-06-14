package app

import boofcv.io.image.UtilImageIO
import convolution.Convolution
import convolution.Kernel
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

fun startSeqPipeline(
    inputDir: File,
    convolution: Convolution,
    kernel: Kernel,
) {
    val projectDir = File(System.getProperty("rootProjectDir"))
    val outputDir = File(projectDir, "output_images")
    if (!outputDir.exists()) outputDir.mkdirs()

    inputDir.listFiles { f -> Files.isRegularFile(f.toPath()) }?.forEach { file ->
        val img = UtilImageIO.loadImage(file.absolutePath) ?: error("Can't load image: $file")
        val convolved =
            runBlocking {
                convolution.convolve(img, kernel)
            }
        val out = File(outputDir, file.name)
        ImageIO.write(convolved, "png", out)
    }
}
