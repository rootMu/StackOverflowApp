package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.ByteArrayOutputStream

class HttpImageLoaderTest {

    @Test
    fun loadBitmap_returnsBitmap_whenHttpClientReturnsValidImageBytes() {
        val validBytes = create1x1PngBytes()
        var actualUrl: String? = null

        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                actualUrl = url
                return NetworkResult.Success(validBytes)
            }
        }

        val loader = HttpImageLoader(httpClient)
        val testUrl = "https://example.com/image.png"

        val bitmap = runBlocking {
            loader.loadBitmap(testUrl)
        }

        assertNotNull(bitmap)
        assertEquals(testUrl, actualUrl)
    }

    @Test
    fun loadBitmap_returnsCachedBitmap_onSecondRequest() {
        val validBytes = create1x1PngBytes()
        var callCount = 0

        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                callCount++
                return NetworkResult.Success(validBytes)
            }
        }

        val loader = HttpImageLoader(httpClient)
        val url = "https://example.com/cache-test.png"

        runBlocking {
            val first = loader.loadBitmap(url)
            val second = loader.loadBitmap(url)
            
            assertNotNull(first)
            assertNotNull(second)
            assertSame("Should return the same instance from cache", first, second)
            assertEquals("Network should only be called once", 1, callCount)
        }
    }

    @Test
    fun getCachedBitmap_returnsBitmap_afterSuccessfulLoad() {
        val validBytes = create1x1PngBytes()
        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> = NetworkResult.Success(validBytes)
        }

        val loader = HttpImageLoader(httpClient)
        val url = "https://example.com/direct-cache.png"

        runBlocking {
            assertNull("Initially cache should be empty", loader.getCachedBitmap(url))
            val loaded = loader.loadBitmap(url)
            assertNotNull(loaded)
            assertSame("Cache should return the loaded bitmap", loaded, loader.getCachedBitmap(url))
        }
    }

    @Test
    fun failedImageLoad_doesNotCacheNull() {
        var callCount = 0
        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                callCount++
                return NetworkResult.Error.Network(RuntimeException("fail"))
            }
        }

        val loader = HttpImageLoader(httpClient)
        val url = "https://example.com/fail.png"

        runBlocking {
            assertNull(loader.loadBitmap(url))
            assertNull(loader.loadBitmap(url))
            assertEquals("Network should be called both times if it failed", 2, callCount)
        }
    }

    @Test
    fun loadBitmap_returnsNull_whenHttpClientReturnsError() {
        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                return NetworkResult.Error.Network(RuntimeException("boom"))
            }
        }

        val loader = HttpImageLoader(httpClient)

        val bitmap = runBlocking {
            loader.loadBitmap("https://example.com/error.png")
        }

        assertNull(bitmap)
    }

    @Test
    fun loadBitmap_returnsNull_whenBytesCannotBeDecoded() {
        val invalidBytes = "not-an-image".toByteArray()

        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
                return NetworkResult.Success(invalidBytes)
            }
        }

        val loader = HttpImageLoader(httpClient)

        val bitmap = runBlocking {
            loader.loadBitmap("https://example.com/invalid.png")
        }

        assertNull(bitmap)
    }

    @Test
    fun lruCache_initiallyEmpty_andRespectsDifferentUrls() {
        val validBytes = create1x1PngBytes()
        val httpClient = object : HttpClient {
            override suspend fun get(url: String): NetworkResult<String> = error("Not used")
            override suspend fun getBytes(url: String): NetworkResult<ByteArray> = NetworkResult.Success(validBytes)
        }

        val loader = HttpImageLoader(httpClient)
        val url1 = "https://example.com/1.png"
        val url2 = "https://example.com/2.png"

        runBlocking {
            val b1 = loader.loadBitmap(url1)
            val b2 = loader.loadBitmap(url2)
            
            assertNotNull(b1)
            assertNotNull(b2)
            assertSame(b1, loader.getCachedBitmap(url1))
            assertSame(b2, loader.getCachedBitmap(url2))
        }
    }

    private fun create1x1PngBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        return output.toByteArray()
    }
}
