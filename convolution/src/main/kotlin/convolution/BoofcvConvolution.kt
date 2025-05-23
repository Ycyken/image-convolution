package convolution

import boofcv.alg.filter.convolve.GConvolveImageOps
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.convolve.Kernel2D_F32
import boofcv.struct.image.GrayF32
import java.awt.image.BufferedImage

fun convolveBoofcv(
    image: BufferedImage,
    kernel: Kernel,
): BufferedImage {
    val planar = ConvertBufferedImage.convertFromPlanar(image, null, true, GrayF32::class.java)
    val output = planar.createSameShape()
    val kernel = Kernel2D_F32(kernel.width, kernel.matrix.flatMap { it.asList() }.toFloatArray())

    planar.bands.forEachIndexed { i, band ->
        val convolvedBand = output.getBand(i)
        GConvolveImageOps.convolveNormalized(kernel, band, convolvedBand)
    }

    return ConvertBufferedImage.convertTo(output, null, true)
}
