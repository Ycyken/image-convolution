package kernels

import convolution.Kernel

fun id(size: Int): Kernel {
    val kernel = Kernel(size)
    kernel[size / 2, size / 2] = 1.0F
    return kernel
}

fun boxBlur(size: Int): Kernel {
    val normalizer = 1.0F / (size * size).toFloat()
    return Kernel(Array(size) { FloatArray(size) { 1.0F } }) * normalizer
}

fun motionBlur(size: Int): Kernel {
    val normalizer = (1.0F / size.toFloat())
    return Kernel(Array(size) { y -> FloatArray(size) { x -> if (x == y) 1.0F else 0.0F } }) * normalizer
}

fun gaussianBlur3x3(): Kernel {
    val normalizer = 1.0F / 16.0F
    return Kernel(
        arrayOf(
            floatArrayOf(1.0F, 2.0F, 1.0F),
            floatArrayOf(2.0F, 4.0F, 2.0F),
            floatArrayOf(1.0F, 2.0F, 1.0F),
        ),
    ) * normalizer
}

fun gaussianBlur5x5(): Kernel {
    val normalizer = 1.0F / 256.0F
    return Kernel(
        arrayOf(
            floatArrayOf(1.0F, 4.0F, 6.0F, 4.0F, 1.0F),
            floatArrayOf(4.0F, 16.0F, 24.0F, 16.0F, 4.0F),
            floatArrayOf(6.0F, 24.0F, 36.0F, 24.0F, 6.0F),
            floatArrayOf(4.0F, 16.0F, 24.0F, 16.0F, 4.0F),
            floatArrayOf(1.0F, 4.0F, 6.0F, 4.0F, 1.0F),
        ),
    ) * normalizer
}

fun edges(): Kernel {
    return Kernel(
        arrayOf(
            floatArrayOf(-1.0F, -1.0F, -1.0F),
            floatArrayOf(-1.0F, 8.0F, -1.0F),
            floatArrayOf(-1.0F, -1.0F, -1.0F),
        ),
    )
}

fun embos(): Kernel {
    return Kernel(
        arrayOf(
            floatArrayOf(-1.0F, -1.0F, 0.0F),
            floatArrayOf(-1.0F, 0.0F, 1.0F),
            floatArrayOf(0.0F, 1.0F, 1.0F),
        ),
    )
}

fun sharpen5(): Kernel {
    return Kernel(
        arrayOf(
            floatArrayOf(0.0F, -1.0F, 0.0F),
            floatArrayOf(-1.0F, 5.0F, -1.0F),
            floatArrayOf(0.0F, -1.0F, 0.0F),
        ),
    )
}

fun sharpen8(): Kernel {
    return Kernel(
        arrayOf(
            floatArrayOf(-1.0F, -1.0F, -1.0F),
            floatArrayOf(-1.0F, 9.0F, -1.0F),
            floatArrayOf(-1.0F, -1.0F, -1.0F),
        ),
    )
}
