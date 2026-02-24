package com.example.stackoverflowapp.data.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.net.URL

class HttpUrlConnectionClientTest {

    @Test
    fun `get returns Success when response is 200 and body is non-empty`() = runBlocking {
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com"),
            responseCodeValue = 200,
            bodyBytes = """{"ok":true}""".toByteArray()
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.get("https://example.com")

        Assert.assertTrue(result is NetworkResult.Success)
        Assert.assertEquals("""{"ok":true}""", (result as NetworkResult.Success).data)
    }

    @Test
    fun `getBytes returns Success when response is 200 and bytes are non-empty`() = runBlocking {
        val bytes = byteArrayOf(1, 2, 3)
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com/image"),
            responseCodeValue = 200,
            bodyBytes = bytes
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.getBytes("https://example.com/image")

        Assert.assertTrue(result is NetworkResult.Success)
        Assert.assertEquals(bytes.toList(), (result as NetworkResult.Success).data.toList())
    }

    @Test
    fun `get returns Http error for non-2xx response`() = runBlocking {
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com"),
            responseCodeValue = 404,
            responseMessageValue = "Not Found"
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.get("https://example.com")

        Assert.assertTrue(result is NetworkResult.Error.Http)
        result as NetworkResult.Error.Http
        Assert.assertEquals(404, result.code)
        Assert.assertEquals("Not Found", result.message)
    }

    @Test
    fun `get returns EmptyBody when response body is blank`() = runBlocking {
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com"),
            responseCodeValue = 200,
            bodyBytes = "   ".toByteArray()
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.get("https://example.com")

        Assert.assertTrue(result is NetworkResult.Error.EmptyBody)
    }

    @Test
    fun `getBytes returns EmptyBody when response body is empty`() = runBlocking {
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com"),
            responseCodeValue = 200,
            bodyBytes = byteArrayOf()
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.getBytes("https://example.com")

        Assert.assertTrue(result is NetworkResult.Error.EmptyBody)
    }

    @Test
    fun `get returns Network error when connection throws`() = runBlocking {
        val connection = FakeHttpURLConnection(
            url = URL("https://example.com"),
            connectThrows = IOException("timeout")
        )

        val client = HttpUrlConnectionClient(
            connectionFactory = FakeHttpConnectionFactory(connection)
        )

        val result = client.get("https://example.com")

        Assert.assertTrue(result is NetworkResult.Error.Network)
    }
}