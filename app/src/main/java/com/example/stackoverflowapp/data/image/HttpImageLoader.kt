package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult

class HttpImageLoader(
    private val httpClient: HttpClient
): ImageLoader {

    override suspend fun loadBitmap(url: String): Bitmap? {
        return when (val result = httpClient.getBytes(url)) {
            is NetworkResult.Success -> {
                BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
            }
            is NetworkResult.Error -> null
        }
    }
}