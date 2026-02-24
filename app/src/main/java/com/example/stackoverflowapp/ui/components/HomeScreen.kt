package com.example.stackoverflowapp.ui.components

import androidx.compose.runtime.Composable
import com.example.stackoverflowapp.data.image.HttpImageLoader
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.data.network.HttpUrlConnectionClient
import com.example.stackoverflowapp.ui.home.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    imageLoader: ImageLoader,
    onRefresh: () -> Unit
) {
    when(uiState) {
        HomeUiState.Loading -> LoadingScreen()

        HomeUiState.Empty -> EmptyStateView(
            title = "No users found",
            message = "No StackOverflow users were returned."
        )

        is HomeUiState.Error -> ErrorStateView(
            message = uiState.message,
            onRetry = onRefresh
        )

        is HomeUiState.Success -> UsersPolaroidGridView(
            users = uiState.users,
            imageLoader = imageLoader
        )
    }
}