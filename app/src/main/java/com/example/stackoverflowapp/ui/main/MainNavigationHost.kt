package com.example.stackoverflowapp.ui.main

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.home.HomeDestination

@Composable
fun MainNavigationHost() {

    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            HomeDestination.register(this, navController, this@SharedTransitionLayout)
            UserDetailsDestination.register(this, navController, this@SharedTransitionLayout)
        }
    }
}