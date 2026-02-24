package com.example.stackoverflowapp.data.network

import java.net.HttpURLConnection

class FakeHttpConnectionFactory(
    private val connection: HttpURLConnection
) : HttpConnectionFactory {
    override fun open(url: String): HttpURLConnection = connection
}