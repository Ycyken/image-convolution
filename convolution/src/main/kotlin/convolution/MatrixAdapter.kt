package convolution

import boofcv.struct.image.GrayU8

class MatrixAdapter(val gray: GrayU8) : Matrix<Int> {
    override val width: Int = gray.width
    override val height: Int = gray.height

    override operator fun get(
        x: Int,
        y: Int,
    ): Int = gray.get(x, y)

    override operator fun set(
        x: Int,
        y: Int,
        value: Int,
    ) = gray.set(x, y, value)

    override fun unsafeGet(
        x: Int,
        y: Int,
    ): Int = gray.unsafe_get(x, y)

    override fun unsafeSet(
        x: Int,
        y: Int,
        value: Int,
    ) = gray.unsafe_set(x, y, value)

    override fun forEachIndexed(action: (Int, Int, Int) -> Unit) = gray.forEachPixel(action)
}
