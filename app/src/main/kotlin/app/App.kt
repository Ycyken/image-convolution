package app

import boofcv.io.image.UtilImageIO
import convolution.ConvMode
import convolution.Convolution
import kernels.boxBlur
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

    val kernel = boxBlur(21)
    val convolution = Convolution(ConvMode.ParallelRows(5, 16u))
    val convolved = convolution.convolve(image, kernel)

    val outputFile = File(projectDir, "output_image")
    ImageIO.write(convolved, "bmp", outputFile)
}
