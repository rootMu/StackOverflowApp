package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.home.HomeUiState

@Composable
fun HomeScreen(
    gridState: LazyGridState,
    uiState: HomeUiState,
    users: List<User>,
    searchQuery: String,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onFollowClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {

    when (uiState) {
        HomeUiState.Loading -> LoadingScreen()

        HomeUiState.Empty -> EmptyStateView(
            title = "No users found \uD83D\uDD0D",
            message = "No StackOverflow users were returned."
        )

        is HomeUiState.Error -> ErrorStateView(
            title = "404 - Users Not Found \uD83D\uDD75\uFE0F\u200D♂\uFE0F",
            message = "We couldn't connect to StackOverflow. Please check your connection and try again.",
            technicalDetails = uiState.message,
            onRetry = onRetry
        )

        is HomeUiState.Success -> if (users.isEmpty() && searchQuery.isNotBlank()) {
            EmptyStateView(
                title = "No users found \uD83D\uDD0D",
                message = "We couldn't find any users matching '$searchQuery'."
            )
        } else {
            UsersPolaroidGridView(
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
}