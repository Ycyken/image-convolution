package app

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import convolution.convolveBoofcv
import kernels.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import startPipeline
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.Path

val filterMap = mapOf(
    "id" to id(1),
    "boxBlur" to boxBlur(13),
    "motionBlur" to motionBlur(13),
    "gaussianBlur3x3" to gaussianBlur3x3(),
    "gaussianBlur5x5" to gaussianBlur5x5(),
    "edges" to edges(),
    "sharpen5" to sharpen5(),
    "sharpen8" to sharpen8(),
    "embos" to embos()
)

fun main(args: Array<String>) {
    val parser = ArgParser("ImageFilterApp")

    val path by parser.argument(ArgType.String, description = "Path to file or directory")
    val filterName by parser.argument(ArgType.String, description = "Filter to apply")

    parser.parse(args)

    val filter = filterMap[filterName] ?: run {
        println("Unsupported filter: $filterName")
        println("Allowed filters: ${filterMap.keys.joinToString(", ")}")
        return
    }

    val projectDir = File(System.getProperty("rootProjectDir"))
    val targetFile = File(path)
    val file = if (targetFile.isAbsolute) targetFile else File(projectDir, path)

    if (!file.exists()) {
        println("Path does not exist: $path")
        return
    }

    val convolution = Convolution(ConvMode.ParallelRows(5))
    if (file.isFile) {
        val image = UtilImageIO.loadImage(file.absolutePath)!!
        val convolved = convolution.convolve(image, filter)
        val outputFile = File(projectDir, "output_image")
        ImageIO.write(convolved, "bmp", outputFile)
    } else if (file.isDirectory) {
        startPipeline(file, convolution, filter)
    } else {
        println("Invalid path: $path is neither file nor directory")
    }
}