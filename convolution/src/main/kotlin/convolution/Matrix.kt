package convolution

interface ReadableMatrix<out T> {
    val width: Int
    val height: Int

    operator fun get(
        x: Int,
        y: Int,
    ): T

    fun unsafeGet(
        x: Int,
        y: Int,
    ): T = get(x, y)

    fun forEachIndexed(action: (x: Int, y: Int, value: T) -> Unit)
}

interface WritableMatrix<in T> {
    val width: Int
    val height: Int

    operator fun set(
        x: Int,
        y: Int,
        value: T,
    )

    fun unsafeSet(
        x: Int,
        y: Int,
        value: T,
    ) = set(x, y, value)
}

interface Matrix<T> : ReadableMatrix<T>, WritableMatrix<T>
