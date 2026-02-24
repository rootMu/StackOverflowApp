package com.example.stackoverflowapp.data.image

import android.graphics.Bitmap

interface ImageLoader {
    suspend fun loadBitmap(url: String): Bitmap?
}