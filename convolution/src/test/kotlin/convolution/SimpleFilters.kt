package convolution

import boofcv.struct.image.GrayU8
import kernels.id
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class SimpleFilters {
    @Test
    fun `id filter doesn't change matrix`() {
        val input = GrayU8(Array(15) { ByteArray(15) { i -> i.toByte() } })
        val kernelId = id(3)

        val convolved = runBlocking { input.convolve(kernelId) }
        assertArrayEquals(
            input.data,
            convolved.data,
        )
    }

    @Test
    fun `shift filter is correct`() {
        val input = GrayU8(Array(5) { ByteArray(5) { i -> i.toByte() } })
        val kernelId = Kernel(3)
        kernelId[2, 1] = 1F

        val actual = runBlocking { input.convolve(kernelId) }
        val expected =
            GrayU8(Array(5) { ByteArray(5) { i -> if (i == 4) 0 else (i + 1).toByte() } })
        assertArrayEquals(
            expected.data,
            actual.data,
        )
    }

    @Test
    fun `blur filter is correct`() {
        val input =
            GrayU8(
                arrayOf(
                    byteArrayOf(1, 10, 1, 10, 1),
                    byteArrayOf(11, 11, 11, 11, 11),
                    byteArrayOf(5, 5, 5, 5, 5),
                    byteArrayOf(10, 10, 10, 10, 10),
                    byteArrayOf(6, 6, 6, 6, 6),
                ),
            )
        val blurKernel =
            Kernel(
                arrayOf(
                    floatArrayOf(0.0F, 0.2F, 0.0F),
                    floatArrayOf(0.2F, 0.2F, 0.2F),
                    floatArrayOf(0.0F, 0.2F, 0.0F),
                ),
            )

        val actual = runBlocking { input.convolve(blurKernel) }
        val expected =
            GrayU8(
                arrayOf(
                    byteArrayOf(7, 6, 8, 6, 7),
                    byteArrayOf(7, 10, 8, 10, 7),
                    byteArrayOf(8, 7, 7, 7, 8),
                    byteArrayOf(8, 8, 8, 8, 8),
                    byteArrayOf(7, 7, 7, 7, 7),
                ),
            )

        assertArrayEquals(
            expected.data,
            actual.data,
        )
    }
}
