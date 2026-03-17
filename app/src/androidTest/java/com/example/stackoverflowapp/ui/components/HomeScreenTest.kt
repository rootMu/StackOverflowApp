package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.home.HomeScreen
import com.example.stackoverflowapp.ui.home.HomeUiState
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
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
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    gridState = rememberLazyGridState(),
                    uiState = HomeUiState.Loading,
                    users = emptyList(),
                    searchQuery = "",
                    showFavouritesOnly = false,
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    contentPadding = PaddingValues(),
                    followedUsers = emptySet(),
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("Fetching Legends...").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    gridState = rememberLazyGridState(),
                    uiState = HomeUiState.Empty,
                    users = emptyList(),
                    searchQuery = "",
                    showFavouritesOnly = false,
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    contentPadding = PaddingValues(),
                    followedUsers = emptySet(),
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("No users found \uD83D\uDD0D").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry_andInvokesCallback() {
        var retryCount = 0

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    gridState = rememberLazyGridState(),
                    uiState = HomeUiState.Error("Network down"),
                    users = emptyList(),
                    searchQuery = "",
                    showFavouritesOnly = false,
                    imageLoader = fakeImageLoader,
                    onRetry = { retryCount++ },
                    onUserClick = {},
                    onFollowClick = {},
                    contentPadding = PaddingValues(),
                    followedUsers = emptySet(),
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("404 - Users Not Found \uD83D\uDD75\uFE0F\u200D♂\uFE0F").assertIsDisplayed()
        composeRule.onNodeWithText("Error code: Network down").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").performClick()

        Assert.assertEquals(1, retryCount)
    }

    @Test
    fun successState_showsUserNames() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001),
            createTestUser(id = 2, name = "Joel Spolsky", reputation = 8000)
        )

        composeRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "test") { _ ->
                    HomeScreen(
                        gridState = rememberLazyGridState(),
                        uiState = HomeUiState.Success(users, emptySet()),
                        users = users,
                        searchQuery = "",
                        showFavouritesOnly = false,
                        imageLoader = fakeImageLoader,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        onRetry = {},
                        onUserClick = {},
                        onFollowClick = {},
                        contentPadding = PaddingValues(),
                        followedUsers = emptySet()
                    )
                }
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }

    @Test
    fun polaroidGrid_showsInitialsPlaceholder_whenNoImageUrl() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001)
        )

        composeRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "test") { _ ->
                    UsersPolaroidGridView(
                        gridState = rememberLazyGridState(),
                        users = users,
                        imageLoader = fakeImageLoader,
                        followedUsers = emptySet(),
                        onUserClick = {},
                        onFollowClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        contentPadding = PaddingValues()
                    )
                }
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("9k").assertIsDisplayed()
    }
}
