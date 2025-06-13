package convolution

import java.awt.image.BufferedImage

enum class ImageType {
    GRAY,
    RGB,
    UNKNOWN,
}

internal fun detectImageType(image: BufferedImage): ImageType {
    val numComponents = image.colorModel.numComponents
    val bits = image.sampleModel.getSampleSize(0)

    return when {
        numComponents == 1 && bits == 8 -> ImageType.GRAY
        numComponents == 3 && bits == 8 -> ImageType.RGB
        else -> ImageType.UNKNOWN
    }
}
