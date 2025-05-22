package app

import boofcv.io.image.UtilImageIO
import convolution.FloatMatrix
import convolution.convolve
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

    val kernel =
        FloatMatrix(Array(9) { y -> FloatArray(9) { x -> 1 / 81F } })
    val convolved = convolve(image, kernel)

    val outputFile = File(projectDir, "output_image")
    ImageIO.write(convolved, "bmp", outputFile)
}
