package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun loadingScreen_displaysCircularProgress() {
        composeRule.setContent { LoadingScreen() }

        composeRule.onNodeWithContentDescription("App Logo").assertIsDisplayed()
        composeRule.onNodeWithText("Fetching Legends...").assertIsDisplayed()
    }

    @Test
    fun errorStateView_displaysMessageAndRetryButton() {
        var retryClicked = false
        composeRule.setContent {
            ErrorStateView(
                title = "Oh Noes...",
                message = "Timeout Error",
                onRetry = { retryClicked = true })
        }

        composeRule.onNodeWithText("Timeout Error").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").performClick()
        Assert.assertTrue(retryClicked)
    }

    @Test
    fun emptyStateView_displaysNoUsersFound() {
        composeRule.setContent { EmptyStateView("No users found", "No Users Found") }
        composeRule.onNodeWithText("No users found").assertIsDisplayed()
    }

    @Test
    fun usersPolaroidGridView_rendersUserCards() {
        val users = listOf(
            User(1, "Jeff Atwood", 9001, null),
            User(2, "Joel Spolsky", 8000, null)
        )

        composeRule.setContent {
            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                followedUserIds = emptySet(),
                onFollowClick = {},
                imageLoader = fakeImageLoader,
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }
}