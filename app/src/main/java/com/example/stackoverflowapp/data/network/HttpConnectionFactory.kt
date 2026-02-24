package com.example.stackoverflowapp.data.network

import java.net.HttpURLConnection
import java.net.URL

interface HttpConnectionFactory {
    fun open(url: String): HttpURLConnection
}

class DefaultHttpConnectionFactory : HttpConnectionFactory {
    override fun open(url: String): HttpURLConnection {
        return URL(url).openConnection() as HttpURLConnection
    }
}