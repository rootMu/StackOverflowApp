package com.example.stackoverflowapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stackoverflowapp.StackOverflowApp
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.ui.components.HomeRoute
import com.example.stackoverflowapp.ui.home.HomeViewModel
import com.example.stackoverflowapp.ui.home.HomeViewModelFactory
import com.example.stackoverflowapp.ui.theme.StackOverflowTheme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as StackOverflowApp).container
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(appContainer.userRepository, appContainer.userStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StackOverflowTheme {
                HomeRoute(viewModel = viewModel, imageLoader = appContainer.imageLoader)
            }
        }
    }
}

