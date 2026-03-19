package com.example.stackoverflowapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UsersPolaroidGridViewTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun user_notFollowed_showsFollowContentDescription() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001)
        )

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                UsersPolaroidGridView(
                    gridState = rememberLazyGridState(),
                    users = users,
                    followedUsers = emptySet(),
                    onFollowClick = {},
                    imageLoader = fakeImageLoader,
                    contentPadding = PaddingValues(),
                    onUserClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule
            .onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Follow Jeff Atwood")
    }

    @Test
    fun user_followed_showsUnfollowContentDescription() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001)
        )

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                UsersPolaroidGridView(
                    gridState = rememberLazyGridState(),
                    users = users,
                    followedUsers = setOf(1),
                    onFollowClick = {},
                    imageLoader = fakeImageLoader,
                    contentPadding = PaddingValues(),
                    onUserClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule
            .onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Unfollow Jeff Atwood")
    }

    @Test
    fun clicking_follow_icon_callsCallbackWithUserId() {
        val users = listOf(
            createTestUser(id = 42, name = "Test User", reputation = 99)
        )

        var clickedId: Int? = null

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                UsersPolaroidGridView(
                    gridState = rememberLazyGridState(),
                    users = users,
                    followedUsers = emptySet(),
                    onFollowClick = { id -> clickedId = id },
                    imageLoader = fakeImageLoader,
                    contentPadding = PaddingValues(),
                    onUserClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule
            .onNodeWithTag("follow_button_42", useUnmergedTree = true)
            .performClick()

        assertEquals(42, clickedId)
    }

    @Test
    fun clicking_follow_updatesUiWhenStateChanges() {
        val users = listOf(
            createTestUser(id = 1, name = "Jeff Atwood", reputation = 9001)
        )

        val followedIdsState = mutableStateOf(setOf<Int>())

        composeRule.setContent {
            val followedIds by followedIdsState

            SharedTransitionTestContext { animatedScope ->
                UsersPolaroidGridView(
                    gridState = rememberLazyGridState(),
                    users = users,
                    followedUsers = followedIds,
                    onFollowClick = { userId ->
                        followedIdsState.value = followedIds
                            .toMutableSet()
                            .apply {
                                if (!add(userId)) remove(userId)
                            }
                    },
                    imageLoader = fakeImageLoader,
                    contentPadding = PaddingValues(),
                    onUserClick = {},
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope
                )
            }
        }

        composeRule
            .onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertContentDescriptionEquals("Follow Jeff Atwood")

        composeRule
            .onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag("follow_button_1", useUnmergedTree = true)
            .assertContentDescriptionEquals("Unfollow Jeff Atwood")

        assertTrue("State should contain user ID 1", followedIdsState.value.contains(1))
    }
}
