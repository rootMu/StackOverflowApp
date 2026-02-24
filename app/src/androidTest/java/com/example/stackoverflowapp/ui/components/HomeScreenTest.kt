package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
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
    }

    @Test
    fun loadingState_showsLoadingMessage() {
        composeRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Loading,
                imageLoader = fakeImageLoader,
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Loading users...").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Empty,
                imageLoader = fakeImageLoader,
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("No users found").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry_andInvokesCallback() {
        var retryCount = 0

        composeRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Error("Network down"),
                imageLoader = fakeImageLoader,
                onRefresh = { retryCount++ }
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
                uiState = HomeUiState.Success(users),
                imageLoader = fakeImageLoader,
                onRefresh = {}
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
                users = users,
                imageLoader = fakeImageLoader,
            )
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("9k").assertIsDisplayed()
    }
}