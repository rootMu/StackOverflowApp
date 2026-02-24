package com.example.stackoverflowapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.home.HomeViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        imageLoader = imageLoader,
        onRefresh = viewModel::loadUsers
    )
}