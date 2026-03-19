package com.example.stackoverflowapp.ui.home

import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.main.createViewModel
import com.example.stackoverflowapp.ui.navigation.Destination

object HomeDestination : Destination {

    override val route = "home"

    override fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        sharedTransitionScope: SharedTransitionScope
    ) {
        navGraphBuilder.composable(
            route = route,
        ) {
            val viewModel: HomeViewModel = createViewModel {
                HomeViewModel(
                    userRepository = it.userRepository,
                    followedUsersRepository = it.followedUsersRepository
                )
            }

            HomeRoute(
                viewModel = viewModel,
                imageLoader = LocalAppContainer.current.imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = this@composable,
                onUserClick = { userId ->
                    navController.navigate(UserDetailsDestination.createRoute(userId))
                })
        }
    }
}