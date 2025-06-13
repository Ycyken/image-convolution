package convolution

internal fun <T> validateConvolutionArgs(
    input: ReadableMatrix<T>,
    output: WritableMatrix<T>,
) {
    require(input.width == output.width && input.height == output.height) { "Input matrix must be same size as output matrix" }
}

internal fun <T : Number> convolvePoint(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
    x: Int,
    y: Int,
) {
    val kernelCenter = kernel.width / 2
    var convolvedValue = 0.0F
    var usedWeights = 0.0F
    kernel.forEachIndexed { kernelX, kernelY, kernelValue ->
        val currX = x + (kernelX - kernelCenter)
        val currY = y + (kernelY - kernelCenter)
        val correspondingValue =
            if (currX in 0 until input.width && currY in 0 until input.height) {
                usedWeights += kernelValue
                input.unsafeGet(currX, currY).toFloat()
            } else {
                0.0F
            }
        convolvedValue += correspondingValue * kernelValue
    }

    if (usedWeights != 0.0F) {
        convolvedValue /= usedWeights
    }
    val transformedValue = transform(convolvedValue)
    output.unsafeSet(x, y, transformedValue)
}

internal fun <T : Number> convolveSeq(
    input: ReadableMatrix<T>,
    kernel: Kernel,
    output: WritableMatrix<T>,
    transform: (Float) -> T,
) {
    validateConvolutionArgs(input, output)

    input.forEachIndexed { x, y, _ ->
        convolvePoint(input, kernel, output, transform, x, y)
    }
}
