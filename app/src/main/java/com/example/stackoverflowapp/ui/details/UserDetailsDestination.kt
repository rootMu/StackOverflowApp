package com.example.stackoverflowapp.ui.details

import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.ui.main.createViewModel
import com.example.stackoverflowapp.ui.navigation.Destination

object UserDetailsDestination : Destination {

    const val ARG_USER_ID = "userId"

    override val route = "details/{$ARG_USER_ID}"

    override fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        sharedTransitionScope: SharedTransitionScope
    ) {
        navGraphBuilder.composable(
            route = route,
            arguments = listOf(
                navArgument(ARG_USER_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: error("User Id is required")

            val viewModel: UserDetailsViewModel = createViewModel(
                key = "details_$userId"
            ) {
                UserDetailsViewModel(
                    userId = userId,
                    userRepository = it.userRepository,
                    followedUsersRepository = it.followedUsersRepository
                )
            }

            UserDetailsRoute(
                viewModel = viewModel,
                imageLoader = LocalAppContainer.current.imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = this@composable,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}