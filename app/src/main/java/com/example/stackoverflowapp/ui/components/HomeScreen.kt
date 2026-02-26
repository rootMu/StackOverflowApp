package com.example.stackoverflowapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.home.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onFollowClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    when (uiState) {
        HomeUiState.Loading -> LoadingScreen()

        HomeUiState.Empty -> EmptyStateView(
            title = "No users found",
            message = "No StackOverflow users were returned."
        )

        is HomeUiState.Error -> ErrorStateView(
            message = uiState.message,
            onRetry = onRetry
        )

        is HomeUiState.Success -> UsersPolaroidGridView(
            users = uiState.users,
            followedUserIds = uiState.followedUserIds,
            modifier = modifier,
            onFollowClick = onFollowClick,
            imageLoader = imageLoader
        )
    }
}