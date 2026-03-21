package com.example.stackoverflowapp.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.domain.model.AppError

/**
 * Top-level screen that observes the [com.example.stackoverflowapp.domain.ErrorBus] and displays global UI elements like Snackbars.
 */
@Composable
fun MainScreen() {
    val container = LocalAppContainer.current
    val errorBus = container.errorBus
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        errorBus.errors.collect { error ->
            val message = when (error) {
                is AppError.Network.NoConnection -> "No internet connection."
                is AppError.Network.ServerError -> "Server is currently down."
                is AppError.Network.Timeout -> "Request timed out."
                is AppError.Network.Unauthorized -> "Session expired. Please log in again."
                is AppError.Data.MalformedResponse -> "Invalid data received from server."
                is AppError.Data.NotFound -> "The requested resource was not found."
                else -> "An unexpected error occurred."
            }

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        MainNavigationHost(
            modifier = Modifier.padding(paddingValues)
        )
    }
}
