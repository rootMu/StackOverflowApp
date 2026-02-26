package com.example.stackoverflowapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.home.HomeViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        HomeScreen(
            uiState = uiState,
            imageLoader = imageLoader,
            onRetry = viewModel::loadUsers,
            onFollowClick = viewModel::toggleFollow
        )
    }
}