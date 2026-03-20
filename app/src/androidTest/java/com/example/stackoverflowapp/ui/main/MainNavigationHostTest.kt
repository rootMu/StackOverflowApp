package com.example.stackoverflowapp.ui.main

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.home.HomeDestination
import com.example.stackoverflowapp.fakes.FakeFollowUserRepository
import com.example.stackoverflowapp.fakes.FakeUserRepository
import com.example.stackoverflowapp.fakes.FakeUserStore
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class MainNavigationHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    private val fakeUserRepo = FakeUserRepository(Result.success(listOf(
        createTestUser(id = 1, name = "Jeff Atwood")
    )))
    
    private val fakeFollowRepo = FakeFollowUserRepository(FakeUserStore())

    private val fakeImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    }

    private val fakeContainer = object : AppContainer {
        override val userRepository = fakeUserRepo
        override val followedUsersRepository = fakeFollowRepo
        override val imageLoader = fakeImageLoader
        override val userDatabase get() = error("not used")
    }

    @Test
    fun mainNavigationHost_startDestination_isHome() {
        setTestContent()

        composeRule.runOnIdle {
            assertEquals(
                HomeDestination.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }

    @Test
    fun clickingUserCard_navigatesToDetailsWithCorrectId() {
        setTestContent()

        composeRule.onNodeWithText("Jeff Atwood").assertIsDisplayed()

        composeRule.onNodeWithText("Jeff Atwood").performClick()

        composeRule.runOnIdle {
            assertEquals(
                UserDetailsDestination.route,
                navController.currentBackStackEntry?.destination?.route
            )
            val idArg = navController.currentBackStackEntry?.arguments?.getInt(UserDetailsDestination.ARG_USER_ID)
            assertEquals(1, idArg)
        }
    }

    @Test
    fun navigatingBackFromDetails_returnsToHome() {
        setTestContent()

        composeRule.runOnIdle {
            navController.navigate(UserDetailsDestination.createRoute(1))
        }

        composeRule.runOnIdle {
            assertEquals(UserDetailsDestination.route, navController.currentBackStackEntry?.destination?.route)
        }

        composeRule.runOnIdle {
            navController.popBackStack()
        }

        composeRule.runOnIdle {
            assertEquals(HomeDestination.route, navController.currentBackStackEntry?.destination?.route)
        }
    }

    private fun setTestContent() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                MainNavigationHost(
                    navController = navController
                )
            }
        }
    }
}
