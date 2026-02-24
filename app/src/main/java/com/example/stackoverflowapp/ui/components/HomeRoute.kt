package com.example.stackoverflowapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.stackoverflowapp.ui.home.HomeViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onRefresh = viewModel::loadUsers
    )
}