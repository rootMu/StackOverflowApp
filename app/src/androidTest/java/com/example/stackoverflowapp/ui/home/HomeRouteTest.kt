package com.example.stackoverflowapp.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.fakes.FakeFollowUserRepository
import com.example.stackoverflowapp.fakes.FakeUserRepository
import com.example.stackoverflowapp.fakes.FakeUserStore
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class HomeRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeUserRepo = FakeUserRepository(Result.success(listOf(
        createTestUser(id = 1, name = "Jeff Atwood"),
        createTestUser(id = 2, name = "Joel Spolsky")
    )))
    
    private val fakeFollowRepo = FakeFollowUserRepository(FakeUserStore())
    
    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun homeRoute_showsUsersFromViewModel() {
        val viewModel = HomeViewModel(fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeRoute(
                    viewModel = viewModel,
                    imageLoader = fakeImageLoader,
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope,
                    onUserClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertIsDisplayed()
    }

    @Test
    fun homeRoute_filteringUpdatesList() {
        val viewModel = HomeViewModel(fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeRoute(
                    viewModel = viewModel,
                    imageLoader = fakeImageLoader,
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope,
                    onUserClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Search users...").performTextInput("Jeff")
        
        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()
        composeRule.onNodeWithText("Joel Spolsky").assertDoesNotExist()
    }

    @Test
    fun homeRoute_clickingUser_invokesOnUserClick() {
        var clickedUserId: Int? = null
        val viewModel = HomeViewModel(fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeRoute(
                    viewModel = viewModel,
                    imageLoader = fakeImageLoader,
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope,
                    onUserClick = { clickedUserId = it }
                )
            }
        }

        composeRule.onNodeWithText("Jeff Atwood").performClick()
        assertEquals(1, clickedUserId)
    }

    @Test
    fun homeRoute_clickingFollow_updatesState() {
        val viewModel = HomeViewModel(fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            SharedTransitionTestContext { animatedScope ->
                HomeRoute(
                    viewModel = viewModel,
                    imageLoader = fakeImageLoader,
                    sharedTransitionScope = this,
                    animatedContentScope = animatedScope,
                    onUserClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("follow_button_1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("follow_button_1", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        
        val followedIds = fakeFollowRepo.followedUserIds.value
        assertEquals(setOf(1), followedIds)
    }
}
