package com.example.stackoverflowapp.ui.main

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.home.HomeDestination
import com.example.stackoverflowapp.ui.transitions.LocalSharedTransitionScope

fun NavGraphBuilder.mainNavigationGraph(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope
) {
    HomeDestination.register(this, navController, sharedTransitionScope)
    UserDetailsDestination.register(this, navController, sharedTransitionScope)
}

/**
 * Top-level navigation host for the application.
 *
 * @param navController The navigation controller to manage screen transitions.
 *                      Defaults to [rememberNavController] for production use.
 */
@Composable
fun MainNavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    buildGraph: NavGraphBuilder.(NavHostController, SharedTransitionScope) -> Unit =
        { controller, scope -> mainNavigationGraph(controller, scope) }
) {
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeDestination.route
            ) {
                buildGraph(navController, this@SharedTransitionLayout)
            }
        }
    }
}
