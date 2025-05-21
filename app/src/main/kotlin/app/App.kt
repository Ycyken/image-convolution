package app

import boofcv.io.image.ConvertBufferedImage
import boofcv.io.image.UtilImageIO
import boofcv.struct.image.GrayU8
import convolution.FloatMatrix
import convolution.convolve
import convolution.toMatrix
import java.awt.image.BufferedImage
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
    val grayImage =
        ConvertBufferedImage.convertFromSingle(image, null, GrayU8::class.java)
    val grayMatrix = grayImage.toMatrix()
    val kernel =
        FloatMatrix(Array(9) { y -> FloatArray(9) { x -> 1 / 81F } })
    val convolved = grayMatrix.convolve(kernel).getOrNull()!!

    val theirMatrix = GrayU8(convolved.matrix)
    val imageOutput: BufferedImage = ConvertBufferedImage.convertTo(theirMatrix, null)
    val outputFile = File(projectDir, "output_image")
    ImageIO.write(imageOutput, "bmp", outputFile)
}
