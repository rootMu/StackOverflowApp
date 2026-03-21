package com.example.stackoverflowapp.ui.details

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.BadgeCounts
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.platform.UriHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.test.onNodeWithTag
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext

@OptIn(ExperimentalSharedTransitionApi::class)
class UserDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun userDetails_rendersContentCorrectly() {
        val user = createTestUser(
            id = 1,
            name = "Jon Skeet",
            reputation = 1450000,
            location = "Reading, UK",
            website = "https://csharpindepth.com",
            aboutMe = "Famous developer",
            badgeCounts = BadgeCounts(gold = 10, silver = 20, bronze = 30)
        )

        composeRule.setContent {
            SharedTransitionTestContext {
                UserDetailsScreen(
                    user = user,
                    isFollowed = false,
                    onFollowClick = {},
                    imageLoader = fakeImageLoader
                )
            }
        }

        composeRule.onNodeWithText("Jon Skeet").assertIsDisplayed()
        composeRule.onNodeWithText("1450000").assertIsDisplayed()
        composeRule.onNodeWithText("Reading, UK").assertIsDisplayed()
        composeRule.onNodeWithText("https://csharpindepth.com").assertIsDisplayed()
        composeRule.onNodeWithTag("user_bio", useUnmergedTree = true).assertIsDisplayed()
        
        composeRule.onNodeWithText("10").assertIsDisplayed()
        composeRule.onNodeWithText("20").assertIsDisplayed()
        composeRule.onNodeWithText("30").assertIsDisplayed()
    }

    @Test
    fun clickingWebsite_invokesUriHandler() {
        var openedUri: String? = null
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUri = uri
            }
        }

        val user = createTestUser(id = 1, website = "https://test.com")

        composeRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
                SharedTransitionTestContext {
                    UserDetailsScreen(
                        user = user,
                        isFollowed = false,
                        onFollowClick = {},
                        imageLoader = fakeImageLoader
                    )
                }
            }
        }

        composeRule.onNodeWithText("https://test.com").performClick()
        assertEquals("https://test.com", openedUri)
    }

    @Test
    fun missingWebsiteAndLocation_showsFallbacks() {
        val user = createTestUser(id = 1, location = null, website = null)

        composeRule.setContent {
            SharedTransitionTestContext {
                UserDetailsScreen(
                    user = user,
                    isFollowed = false,
                    onFollowClick = {},
                    imageLoader = fakeImageLoader
                )
            }
        }

        composeRule.onNodeWithText("Unknown Location").assertIsDisplayed()
    }

    @Test
    fun followButton_updatesVisually_whenStateChanges() {
        var followClicked = false
        val user = createTestUser(id = 1, name = "Test User")

        composeRule.setContent {
            SharedTransitionTestContext {
                UserDetailsScreen(
                    user = user,
                    isFollowed = false,
                    onFollowClick = { followClicked = true },
                    imageLoader = fakeImageLoader
                )
            }
        }

        composeRule.onNodeWithText("Follow User").assertIsDisplayed()
        composeRule.onNodeWithText("Follow User").performClick()
        assertEquals(true, followClicked)
    }

    @Test
    fun followedState_showsFollowingText() {
        val user = createTestUser(id = 1, name = "Test User")

        composeRule.setContent {
            SharedTransitionTestContext {
                UserDetailsScreen(
                    user = user,
                    isFollowed = true,
                    onFollowClick = { },
                    imageLoader = fakeImageLoader
                )
            }
        }

        composeRule.onNodeWithText("Following").assertIsDisplayed()
    }
}
