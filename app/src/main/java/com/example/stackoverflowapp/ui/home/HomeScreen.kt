package com.example.stackoverflowapp.ui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.components.EmptyStateView
import com.example.stackoverflowapp.ui.components.ErrorStateView
import com.example.stackoverflowapp.ui.components.LoadingScreen
import com.example.stackoverflowapp.ui.components.UsersPolaroidGridView

@Composable
fun HomeScreen(
    gridState: LazyGridState,
    uiState: HomeUiState,
    users: List<User>,
    followedUsers: Set<Int>,
    searchQuery: String,
    showFavouritesOnly: Boolean,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
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

        is HomeUiState.Success -> if (users.isEmpty()) {

            val title = if (showFavouritesOnly) "No favourites found" else "No users found"

            val message =
                if (searchQuery.isBlank()) "" else "We couldn't find any users matching '$searchQuery'."

            EmptyStateView(
                showFavouritesOnly = showFavouritesOnly,
                title = title,
                message = message
            )
        } else {
            UsersPolaroidGridView(
                gridState = gridState,
                users = users,
                followedUsers = followedUsers,
                modifier = modifier,
                onUserClick = onUserClick,
                onFollowClick = onFollowClick,
                imageLoader = imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                contentPadding = contentPadding
            )
        }
    }
}
