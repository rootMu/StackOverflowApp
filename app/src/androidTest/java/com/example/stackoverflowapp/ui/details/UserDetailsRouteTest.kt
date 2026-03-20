package com.example.stackoverflowapp.ui.details

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.fakes.FakeFollowUserRepository
import com.example.stackoverflowapp.fakes.FakeUserRepository
import com.example.stackoverflowapp.fakes.FakeUserStore
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class UserDetailsRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeUserRepo = FakeUserRepository(Result.success(emptyList()))
    private val fakeFollowRepo = FakeFollowUserRepository(FakeUserStore())
    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    @Test
    fun userDetailsRoute_showsLoadingState() {
        fakeUserRepo.shouldHoldLoading = true

        val viewModel = UserDetailsViewModel(1, fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            UserDetailsRoute(
                viewModel = viewModel,
                imageLoader = fakeImageLoader,
                onBack = {}
            )
        }

        composeRule.onNodeWithText("Fetching Legends...").assertIsDisplayed()
    }

    @Test
    fun userDetailsRoute_showsSuccessState() {
        val user = createTestUser(id = 1, name = "Jon Skeet")
        fakeUserRepo.userDetailsResult = Result.success(user)

        val viewModel = UserDetailsViewModel(1, fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = true, label = "") { animatedState ->
                    if (animatedState) {
                        UserDetailsRoute(
                            viewModel = viewModel,
                            imageLoader = fakeImageLoader,
                            onBack = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Jon Skeet").assertIsDisplayed()
    }

    @Test
    fun clickingBack_invokesOnBackCallback() {
        var backInvoked = false
        val viewModel = UserDetailsViewModel(1, fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            UserDetailsRoute(
                viewModel = viewModel,
                imageLoader = fakeImageLoader,
                onBack = { backInvoked = true }
            )
        }

        composeRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backInvoked)
    }

    @Test
    fun userDetailsRoute_showsErrorState_andInvokesBackOnRetry() {
        var backInvoked = false
        fakeUserRepo.userDetailsResult = Result.failure<User>(Exception("Failed to load"))

        val viewModel = UserDetailsViewModel(1, fakeUserRepo, fakeFollowRepo)

        composeRule.setContent {
            UserDetailsRoute(
                viewModel = viewModel,
                imageLoader = fakeImageLoader,
                onBack = { backInvoked = true }
            )
        }

        composeRule.onNodeWithText("Failed to load").assertIsDisplayed()
        composeRule.onNodeWithText("Try Again").performClick()

        assertTrue(backInvoked)
    }
}
