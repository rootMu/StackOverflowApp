package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream

class HttpImageLoaderTest {

    @Test
    fun loadBitmap_returnsBitmap_whenHttpClientReturnsValidImageBytes() {
        val validBytes = create1x1PngBytes()

        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> {
                error("Not used in this test")
            }

            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                return NetworkResult.Success(validBytes)
            }
        }

        val loader = HttpImageLoader(httpClient)

        val bitmap = runBlocking {
            loader.loadBitmap("https://example.com/image.png")
        }

        assertNotNull(bitmap)
    }

    @Test
    fun loadBitmap_returnsNull_whenHttpClientReturnsError() {
        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> {
                error("Not used in this test")
            }

            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                return NetworkResult.Error.Network(RuntimeException("boom"))
            }
        }

        val loader = HttpImageLoader(httpClient)

        val bitmap = runBlocking {
            loader.loadBitmap("https://example.com/image.png")
        }

        assertNull(bitmap)
    }

    @Test
    fun loadBitmap_returnsNull_whenBytesCannotBeDecoded() {
        val invalidBytes = "not-an-image".toByteArray()

        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> {
                error("Not used in this test")
            }

            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                return NetworkResult.Success(invalidBytes)
            }
        }

        val loader = HttpImageLoader(httpClient)

        val bitmap = runBlocking {
            loader.loadBitmap("https://example.com/image.png")
        }

        assertNull(bitmap)
    }

    private fun create1x1PngBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        return output.toByteArray()
    }
}