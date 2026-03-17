package com.example.stackoverflowapp.ui.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface Destination {
    val route: String

    fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        sharedTransitionScope: SharedTransitionScope
    )
}