package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.ui.home.HomeScreen
import com.example.stackoverflowapp.ui.home.HomeScreenState
import com.example.stackoverflowapp.ui.home.UserUiModel
import com.example.stackoverflowapp.ui.transitions.LocalAnimatedVisibilityScope
import com.example.stackoverflowapp.ui.transitions.LocalSharedTransitionScope
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
                    state = HomeScreenState(isLoading = true),
                    gridState = rememberLazyGridState(),
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("Fetching Legends...").assertIsDisplayed()
    }

    @Test
    fun emptyState_noUsers_showsDefaultMessage() {
        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    state = HomeScreenState(users = emptyList(), searchQuery = ""),
                    gridState = rememberLazyGridState(),
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("No users found").assertIsDisplayed()
        composeRule.onNodeWithText("No StackOverflow users were returned.").assertIsDisplayed()
    }

    @Test
    fun emptyState_withSearchQuery_showsSearchMessage() {
        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    state = HomeScreenState(users = emptyList(), searchQuery = "Batman"),
                    gridState = rememberLazyGridState(),
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("We couldn't find any users matching 'Batman'.").assertIsDisplayed()
    }

    @Test
    fun emptyState_favoritesOnly_showsFavoritesMessage() {
        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    state = HomeScreenState(users = emptyList(), showFavouritesOnly = true),
                    gridState = rememberLazyGridState(),
                    imageLoader = fakeImageLoader,
                    onRetry = {},
                    onUserClick = {},
                    onFollowClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("No favorites found").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry_andInvokesCallback() {
        var retryCount = 0

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeScreen(
                    state = HomeScreenState(error = "Network down"),
                    gridState = rememberLazyGridState(),
                    imageLoader = fakeImageLoader,
                    onRetry = { retryCount++ },
                    onUserClick = {},
                    onFollowClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule.onNodeWithText("Connection Error").assertIsDisplayed()
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
        val uiModels = users.map { UserUiModel(it, false) }

        composeRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "test") { animatedState ->
                    if(animatedState) {
                        CompositionLocalProvider(
                            LocalSharedTransitionScope provides this@SharedTransitionLayout,
                            LocalAnimatedVisibilityScope provides this@AnimatedContent
                        ) {
                            HomeScreen(
                                state = HomeScreenState(users = uiModels),
                                gridState = rememberLazyGridState(),
                                imageLoader = fakeImageLoader,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                onRetry = {},
                                onUserClick = {},
                                onFollowClick = {}
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }
}
