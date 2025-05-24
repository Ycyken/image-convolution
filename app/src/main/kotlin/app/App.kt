package app

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.convolve
import kernels.motionBlur
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Enter the file name")
        return
    }
    val projectDir = File(System.getProperty("rootProjectDir"))
    val sourcePath = projectDir.resolve(args[0]).absoluteFile.toString()
    val image = UtilImageIO.loadImage(sourcePath)!!

    val kernel = motionBlur(9)
    val start = System.currentTimeMillis()
    val convolved = convolve(image, kernel, ConvMode.ParallelRows(1))
    val end = System.currentTimeMillis()
    println("Convolution took ${end - start} ms")

    val outputFile = File(projectDir, "output_image")
    ImageIO.write(convolved, "bmp", outputFile)
}
