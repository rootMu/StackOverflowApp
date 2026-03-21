package com.example.stackoverflowapp.ui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.components.EmptyStateView
import com.example.stackoverflowapp.ui.components.ErrorStateView
import com.example.stackoverflowapp.ui.components.LoadingScreen
import com.example.stackoverflowapp.ui.components.UsersPolaroidGridView

@Composable
fun HomeScreen(
    state: HomeScreenState,
    gridState: LazyGridState,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    when {
        state.isLoading && !state.isRefreshing -> LoadingScreen()
        state.error != null && state.users.isEmpty() -> ErrorStateView(
            title = "Connection Error",
            message = "We couldn't connect to StackOverflow. Please check your connection and try again.",
            technicalDetails = state.error,
            onRetry = onRetry
        )
        state.users.isEmpty() -> {
            val title = if (state.showFavouritesOnly) "No favorites found" else "No users found"
            val message = if (state.searchQuery.isBlank()) 
                "No StackOverflow users were returned." 
            else 
                "We couldn't find any users matching '${state.searchQuery}'."
            
            EmptyStateView(
                showFavouritesOnly = state.showFavouritesOnly,
                title = title,
                message = message
            )
        }
        else -> {
            UsersPolaroidGridView(
                gridState = gridState,
                users = state.users.map { it.user },
                followedUsers = state.users.filter { it.isFollowed }.map { it.user.id }.toSet(),
                modifier = modifier,
                onUserClick = onUserClick,
                onFollowClick = onFollowClick,
                imageLoader = imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                contentPadding = contentPadding,
                isLoadingMore = state.isLoadingMore,
                isEndReached = state.endReached,
                onLoadMore = onLoadMore
            )
        }
    }
}
