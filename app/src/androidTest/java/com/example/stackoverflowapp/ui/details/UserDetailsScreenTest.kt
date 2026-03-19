package com.example.stackoverflowapp.ui.details

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class UserDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun userDetailsScreen_rendersUserInfo() {
        val user = createTestUser(
            name = "Test User",
            reputation = 1234,
            location = "London",
            website = "https://example.com"
        )

        composeRule.setContent {
            UserDetailsScreen(
                user = user,
                isFollowed = false,
                onFollowClick = {},
                imageLoader = fakeImageLoader
            )
        }

        composeRule.onNodeWithText("Test User").assertIsDisplayed()
        composeRule.onNodeWithText("1234").assertIsDisplayed()
        composeRule.onNodeWithText("London").assertIsDisplayed()
        composeRule.onNodeWithText("https://example.com").assertIsDisplayed()
    }

    @Test
    fun userDetailsScreen_clickingFollow_triggersCallback() {
        var followClicked = false
        val user = createTestUser()

        composeRule.setContent {
            UserDetailsScreen(
                user = user,
                isFollowed = false,
                onFollowClick = { followClicked = true },
                imageLoader = fakeImageLoader
            )
        }

        composeRule.onNodeWithText("Follow User").performClick()
        assertTrue(followClicked)
    }

    @Test
    fun userDetailsScreen_showsFollowing_whenIsFollowedIsTrue() {
        val user = createTestUser()

        composeRule.setContent {
            UserDetailsScreen(
                user = user,
                isFollowed = true,
                onFollowClick = {},
                imageLoader = fakeImageLoader
            )
        }

        composeRule.onNodeWithText("Following").assertIsDisplayed()
    }

    @Test
    fun userDetailsScreen_showsAboutMe_whenPresent() {
        val user = createTestUser(
            aboutMe = "This is my bio."
        )

        composeRule.setContent {
            UserDetailsScreen(
                user = user,
                isFollowed = false,
                onFollowClick = {},
                imageLoader = fakeImageLoader
            )
        }

        composeRule.onNodeWithText("About Me").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("user_bio")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("This is my bio.", substring = true)
    }

    @Test
    fun userDetailsScreen_hidesAboutMe_whenNullOrBlank() {
        val user = createTestUser(aboutMe = null)

        composeRule.setContent {
            UserDetailsScreen(
                user = user,
                isFollowed = false,
                onFollowClick = {},
                imageLoader = fakeImageLoader
            )
        }

        composeRule.onNodeWithText("About Me").assertDoesNotExist()
    }
}
