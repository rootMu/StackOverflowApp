package com.example.stackoverflowapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.home.HomeUiState
import com.example.stackoverflowapp.ui.home.HomeViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    var isManuallyExpanded by remember { mutableStateOf(false) }

    val isCollapsed by remember {
        derivedStateOf {
            val scrolled =
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 100
            scrolled && !isManuallyExpanded
        }
    }

    LaunchedEffect(gridState.isScrollInProgress, gridState.firstVisibleItemIndex) {
        if (gridState.isScrollInProgress || (gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0)) {
            isManuallyExpanded = false
        }
    }

    val appBarColor by animateColorAsState(
        targetValue = Color.Transparent,
        label = "AppBarColor"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(appBarColor)
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
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .animateContentSize()
                            .height(56.dp)
                            .then(if (isCollapsed) Modifier.width(56.dp) else Modifier.fillMaxWidth())
                            .clickable(enabled = isCollapsed) { isManuallyExpanded = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing ?: false,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreen(
                gridState = gridState,
                uiState = uiState,
                users = viewModel.filteredUsers,
                searchQuery = viewModel.searchQuery,
                imageLoader = imageLoader,
                onRetry = viewModel::loadUsers,
                onFollowClick = viewModel::toggleFollow,
                contentPadding = paddingValues
            )
        }
    }
}