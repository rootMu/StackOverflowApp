package com.example.stackoverflowapp.ui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.components.FilterRow
import com.example.stackoverflowapp.ui.components.SearchPillBar

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onUserClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState()

    LaunchedEffect(state.searchQuery, state.sortOrder, state.showFavouritesOnly) {
        gridState.scrollToItem(0)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .statusBarsPadding()
            ) {
                Spacer(
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    SearchPillBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                FilterRow(
                    sortOrder = state.sortOrder,
                    showFavouritesOnly = state.showFavouritesOnly,
                    onToggleFavorites = viewModel::toggleFavoritesFilter,
                    onSortOrderChange = viewModel::onSortOrderChange
                )
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeScreen(
                state = state,
                gridState = gridState,
                imageLoader = imageLoader,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onRetry = viewModel::loadUsers,
                onUserClick = onUserClick,
                onFollowClick = viewModel::onFollowClick,
                contentPadding = PaddingValues(0.dp)
            )
        }
    }
}
