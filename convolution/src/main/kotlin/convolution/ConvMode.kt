package convolution

sealed class ConvMode {
    abstract val thrs: UInt

    data class Sequential(override val thrs: UInt = 3u) : ConvMode()

    data class ParallelRows(val batchSize: Int, override val thrs: UInt = 0u) : ConvMode()

    data class ParallelCols(val batchSize: Int, override val thrs: UInt = 0u) : ConvMode()

    data class ParallelRectangle(val width: Int, val height: Int, override val thrs: UInt = 0u) :
        ConvMode()

    data class ParallelElems(override val thrs: UInt = 0u) : ConvMode()
}
