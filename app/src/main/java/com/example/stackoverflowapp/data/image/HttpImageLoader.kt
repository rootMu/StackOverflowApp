package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult

class HttpImageLoader(
    private val httpClient: HttpClient
): ImageLoader {

    private val memoryCache = mutableMapOf<String, Bitmap>()

    override fun getCachedBitmap(url: String): Bitmap? = memoryCache[url]

    override suspend fun loadBitmap(url: String): Bitmap? {
        memoryCache[url]?.let { return it }

        val bitmap = when (val result = httpClient.getBytes(url)) {

            is NetworkResult.Success -> {
                BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
            }
            is NetworkResult.Error -> null
        }

        bitmap?.let {
            memoryCache[url] = it
        }

        return bitmap
    }
}