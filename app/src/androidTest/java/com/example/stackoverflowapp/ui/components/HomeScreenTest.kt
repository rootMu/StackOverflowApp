package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.home.HomeUiState
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun loadingState_showsLoadingMessage() {
        composeRule.setContent {
            HomeScreen(
                gridState = rememberLazyGridState(),
                uiState = HomeUiState.Loading,
                users = emptyList(),
                imageLoader = fakeImageLoader,
                onRetry = {},
                onFollowClick = {},
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("Loading users...").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeRule.setContent {
            HomeScreen(
                gridState = rememberLazyGridState(),
                uiState = HomeUiState.Empty,
                users = emptyList(),
                imageLoader = fakeImageLoader,
                onRetry = {},
                onFollowClick = {},
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("No users found").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry_andInvokesCallback() {
        var retryCount = 0

        composeRule.setContent {
            HomeScreen(
                gridState = rememberLazyGridState(),
                uiState = HomeUiState.Error("Network down"),
                users = emptyList(),
                imageLoader = fakeImageLoader,
                onRetry = { retryCount++ },
                onFollowClick = {},
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("Unable to load users").assertIsDisplayed()
        composeRule.onNodeWithText("Network down").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        Assert.assertEquals(1, retryCount)
    }

    @Test
    fun successState_showsUserNames() {
        val users = listOf(
            User(1, "Jeff Atwood", 9001, null),
            User(2, "Joel Spolsky", 8000, null)
        )

        composeRule.setContent {
            HomeScreen(
                gridState = rememberLazyGridState(),
                uiState = HomeUiState.Success(users),
                users = users,
                imageLoader = fakeImageLoader,
                onRetry = {},
                onFollowClick = {},
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }

    @Test
    fun polaroidGrid_showsInitialsPlaceholder_whenNoImageUrl() {
        val users = listOf(
            User(1, "Jeff Atwood", 9001, null)
        )

        composeRule.setContent {
            UsersPolaroidGridView(
                gridState = rememberLazyGridState(),
                users = users,
                imageLoader = fakeImageLoader,
                followedUserIds = emptySet(),
                onFollowClick = {},
                contentPadding = PaddingValues.Absolute()
            )
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("9k").assertIsDisplayed()
    }
}