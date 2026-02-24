package com.example.stackoverflowapp.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class HttpUrlConnectionClient(
    private val connectionFactory: HttpConnectionFactory = DefaultHttpConnectionFactory()
) : HttpClient {

    override suspend fun get(url: String): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            executeGet(url) { input ->
                input.bufferedReader()
                    .readText()
                    .takeIf { it.isNotBlank() }
            }
        }

    override suspend fun getBytes(url: String): NetworkResult<ByteArray> =
        withContext(Dispatchers.IO) {
            executeGet(url) { input ->
                input.readBytes()
                    .takeIf { it.isNotEmpty() }
            }
        }

    private inline fun <T> executeGet(
        url: String,
        parseBody: (InputStream) -> T?
    ): NetworkResult<T> {
        return runCatching {
            val connection = connectionFactory.open(url).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                doInput = true
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "StackOverflowApp/1.0")
            }

            try {
                connection.connect()

                val code = connection.responseCode
                if (code !in 200..299) {
                    return NetworkResult.Error.Http(
                        code = code,
                        message = connection.responseMessage
                    )
                }

                connection.getInputStream().use(parseBody)?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error.EmptyBody

            } finally {
                connection.disconnect()
            }
        }.getOrElse { throwable ->
            NetworkResult.Error.Network(throwable)
        }
    }
}