package com.example.stackoverflowapp.data.network

interface HttpClient {
    suspend fun get(url: String): NetworkResult<String>
    suspend fun getBytes(url: String): NetworkResult<ByteArray>
}