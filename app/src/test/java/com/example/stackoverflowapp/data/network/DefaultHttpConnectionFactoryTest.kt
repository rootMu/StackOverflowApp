package com.example.stackoverflowapp.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection

class DefaultHttpConnectionFactoryTest {

    private val factory = DefaultHttpConnectionFactory()

    @Test
    fun `open returns HttpURLConnection for valid http url`() {
        val url = "http://example.com"
        val connection = factory.open(url)

        assertNotNull("Connection should not be null", connection)
        @Suppress("USELESS_IS_CHECK")
        assertTrue("Should return a HttpURLConnection instance", connection is HttpURLConnection)
        assertEquals(url, connection.url.toString())
    }

    @Test
    fun `open returns HttpURLConnection for valid https url`() {
        val url = "https://api.stackexchange.com"
        val connection = factory.open(url)

        assertNotNull(connection)
        assertEquals(url, connection.url.toString())
    }

    @Test(expected = java.net.MalformedURLException::class)
    fun `open throws MalformedURLException for invalid protocol`() {
        val invalidUrl = "not_a_url"
        factory.open(invalidUrl)
    }
}