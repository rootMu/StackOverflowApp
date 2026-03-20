package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.stackoverflowapp.data.image.ImageLoader
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test

class AsyncImageWithCrossfadeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsPlaceholder_whenImageIsNull() {
        val fakeImageLoader = object : ImageLoader {
            override suspend fun loadBitmap(url: String): Bitmap? = null
            override fun getCachedBitmap(url: String): Bitmap? = null
        }

        composeRule.setContent {
            AsyncImageWithCrossfade(
                url = null,
                imageLoader = fakeImageLoader,
                displayName = "Test User"
            )
        }

        composeRule.onNodeWithContentDescription("Test User placeholder").assertIsDisplayed()
    }

    @Test
    fun showsPlaceholder_whileImageIsLoading() {
        val loadingDeferred = CompletableDeferred<Bitmap?>()
        val fakeImageLoader = object : ImageLoader {
            override suspend fun loadBitmap(url: String): Bitmap? = loadingDeferred.await()
            override fun getCachedBitmap(url: String): Bitmap? = null
        }

        composeRule.setContent {
            AsyncImageWithCrossfade(
                url = "https://example.com/image.png",
                imageLoader = fakeImageLoader,
                displayName = "Test User"
            )
        }

        composeRule.onNodeWithContentDescription("Test User placeholder").assertIsDisplayed()
    }

    @Test
    fun showsCachedImageImmediately_ifAvailable() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val fakeImageLoader = object : ImageLoader {
            override suspend fun loadBitmap(url: String): Bitmap = bitmap
            override fun getCachedBitmap(url: String): Bitmap = bitmap
        }

        composeRule.setContent {
            AsyncImageWithCrossfade(
                url = "https://example.com/image.png",
                imageLoader = fakeImageLoader,
                displayName = "Test User"
            )
        }

        composeRule.onNodeWithContentDescription("Test User placeholder").assertDoesNotExist()
    }
}
