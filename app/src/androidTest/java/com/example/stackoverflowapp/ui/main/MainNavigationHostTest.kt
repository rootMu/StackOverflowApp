package com.example.stackoverflowapp.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.home.HomeDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    private val fakeContainer = object : AppContainer {
        override val userRepository get() = error("userRepository should not be used in MainNavigationHostTest")
        override val followedUsersRepository get() = error("followedUsersRepository should not be used in MainNavigationHostTest")
        override val imageLoader get() = error("imageLoader should not be used in MainNavigationHostTest")
        override val userDatabase get() = error("userDatabase should not be used in MainNavigationHostTest")
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
    fun mainNavigationHost_navigateToDetails_updatesCurrentRouteAndArgs() {
        setTestContent()

        val targetUserId = 1234

        composeRule.runOnIdle {
            navController.navigate("details/$targetUserId")
        }

        composeRule.runOnIdle {
            assertEquals(
                UserDetailsDestination.route,
                navController.currentBackStackEntry?.destination?.route
            )

            val argValue = navController.currentBackStackEntry
                ?.arguments
                ?.getInt(UserDetailsDestination.ARG_USER_ID)

            assertEquals(targetUserId, argValue)
        }
    }

    @Test
    fun mainNavigationHost_popBackStack_fromDetails_returnsToHome() {
        setTestContent()

        composeRule.runOnIdle {
            navController.navigate("details/42")
        }

        composeRule.runOnIdle {
            navController.popBackStack()
        }

        composeRule.runOnIdle {
            assertEquals(
                HomeDestination.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }

    private fun setTestContent() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                MainNavigationHost(
                    navController = navController,
                    buildGraph = { _, _ ->
                        registerTestGraph()
                    }
                )
            }
        }
    }

    private fun NavGraphBuilder.registerTestGraph() {
        composable(HomeDestination.route) {
            DummyScreen()
        }

        composable(
            route = UserDetailsDestination.route,
            arguments = listOf(
                navArgument(UserDetailsDestination.ARG_USER_ID) {
                    type = NavType.IntType
                }
            )
        ) {
            DummyScreen()
        }
    }

    @Composable
    private fun DummyScreen() = Unit
}
