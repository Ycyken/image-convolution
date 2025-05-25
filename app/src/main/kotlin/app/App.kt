package app

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.convolve
import kernels.boxBlur
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Enter the file name")
        return
    }
    val startLoad = System.currentTimeMillis()
    val projectDir = File(System.getProperty("rootProjectDir"))
    val sourcePath = projectDir.resolve(args[0]).absoluteFile.toString()
    val image = UtilImageIO.loadImage(sourcePath)!!
    val endLoad = System.currentTimeMillis()
    println("Load image took ${endLoad - startLoad} ms")

    val startConv = System.currentTimeMillis()
    val kernel = boxBlur(21)
    val convolved = convolve(image, kernel, ConvMode.ParallelRows(5))
    val endConv = System.currentTimeMillis()
    println("Convolution took ${endConv - startConv} ms")

    val startSave = System.currentTimeMillis()
    val outputFile = File(projectDir, "output_image")
    ImageIO.write(convolved, "bmp", outputFile)
    val endSave = System.currentTimeMillis()
    println("Convolution took ${endSave - startSave} ms")
}
