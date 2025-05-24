package kernels

import convolution.Kernel

fun id(size: Int): Kernel {
    val kernel = Kernel(size)
    kernel[size / 2, size / 2] = 1.0F
    return kernel
}

fun boxBlur(size: Int): Kernel {
    val normalizer = (size * size).toFloat()
    return Kernel(Array(size) { FloatArray(size) { 1.0F / normalizer } })
}

fun motionBlur(size: Int): Kernel {
    val normalizer = size.toFloat()
    return Kernel(Array(size) { y -> FloatArray(size) { x -> if (x == y) 1.0F / normalizer else 0.0F } })
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
