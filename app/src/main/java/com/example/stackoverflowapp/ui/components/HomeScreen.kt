package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.home.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    users: List<User>,
    gridState: LazyGridState,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onFollowClick: (Int) -> Unit,
    contentPadding: PaddingValues,
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
            gridState = gridState,
            users = users,
            followedUserIds = uiState.followedUserIds,
            modifier = modifier,
            onFollowClick = onFollowClick,
            imageLoader = imageLoader,
            contentPadding = contentPadding
        )
    }
}