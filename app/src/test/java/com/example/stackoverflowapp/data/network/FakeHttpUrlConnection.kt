package com.example.stackoverflowapp.data.network

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FakeHttpURLConnection(
    url: URL,
    private val responseCodeValue: Int = 200,
    private val responseMessageValue: String = "OK",
    private val bodyBytes: ByteArray = byteArrayOf(),
    private val connectThrows: Throwable? = null
) : HttpURLConnection(url) {

    var disconnected = false
        private set

    override fun connect() {
        connectThrows?.let { throw it }
    }

    override fun disconnect() {
        disconnected = true
    }

    override fun usingProxy(): Boolean = false

    override fun getResponseCode(): Int = responseCodeValue

    override fun getResponseMessage(): String = responseMessageValue

    override fun getInputStream(): InputStream = ByteArrayInputStream(bodyBytes)
}