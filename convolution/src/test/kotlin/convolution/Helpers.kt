package convolution

import boofcv.io.image.UtilImageIO
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import kotlin.math.abs

fun getImgResource(name: String): BufferedImage {
    val url =
        TestConvolution::class.java.classLoader.getResource("all_images/$name")?.file
            ?: error("Cannot load image $name")
    return UtilImageIO.loadImage(url) ?: error("Cannot load image $name")
}

fun assertImgEquals(
    img1: BufferedImage,
    img2: BufferedImage,
    tolerance: Int = 1,
) {
    assert(img1.width == img2.width)
    assert(img1.height == img2.height)

    for (y in 0 until img1.height) {
        for (x in 0 until img1.width) {
            val rgb1 = img1.getRGB(x, y)
            val rgb2 = img2.getRGB(x, y)

            val r1 = (rgb1 shr 16) and 0xFF
            val g1 = (rgb1 shr 8) and 0xFF
            val b1 = rgb1 and 0xFF

            val r2 = (rgb2 shr 16) and 0xFF
            val g2 = (rgb2 shr 8) and 0xFF
            val b2 = rgb2 and 0xFF

            if (abs(r1 - r2) > tolerance || abs(g1 - g2) > tolerance || abs(b1 - b2) > tolerance) {
                println("Difference at ($x, $y): ($r1, $g1, $b1) vs ($r2, $g2, $b2)")
            }
            assert(abs(r1 - r2) <= tolerance)
            assert(abs(g1 - g2) <= tolerance)
            assert(abs(b1 - b2) <= tolerance)
        }
    }
}

fun assertConvolutions(
    image: BufferedImage,
    kernel: Kernel,
    convolution1: Convolution,
    convolve2: (BufferedImage, Kernel) -> BufferedImage,
    tolerance: Int = 1,
) {
    val convolution = Convolution(ConvMode.Sequential())
    val convolved1 = runBlocking { convolution1.convolve(image, kernel) }
    val convolved2 = convolve2(image, kernel)
    assertImgEquals(convolved1, convolved2, tolerance)
}
