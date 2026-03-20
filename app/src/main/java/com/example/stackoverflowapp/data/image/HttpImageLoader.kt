package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult

class HttpImageLoader(
    private val httpClient: HttpClient
): ImageLoader {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    override fun getCachedBitmap(url: String): Bitmap? = memoryCache.get(url)

    override suspend fun loadBitmap(url: String): Bitmap? {
        memoryCache.get(url)?.let { return it }

        val bitmap = when (val result = httpClient.getBytes(url)) {
            is NetworkResult.Success -> {
                BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
            }
            is NetworkResult.Error -> null
        }

        bitmap?.let {
            memoryCache.put(url, it)
        }

        return bitmap
    }
}
