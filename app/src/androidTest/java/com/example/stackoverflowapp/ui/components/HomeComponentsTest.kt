package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
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
        composeRule.setContent {
            EmptyStateView(
                title = "No users found",
                message = "No Users Found"
            )
        }
        composeRule.onNodeWithText("No users found").assertIsDisplayed()
    }

    @Test
    fun usersPolaroidGridView_rendersUserCards() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001),
            createTestUser(id = 2, name = "Joel Spolsky", reputation = 8000)
        )

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                UsersPolaroidGridView(
                    gridState = rememberLazyGridState(),
                    users = users,
                    followedUsers = emptySet(),
                    onUserClick = {},
                    onFollowClick = {},
                    imageLoader = fakeImageLoader,
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope,
                    contentPadding = PaddingValues()
                )
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }
}
