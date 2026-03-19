package com.example.stackoverflowapp.ui.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.ui.components.ErrorStateView
import com.example.stackoverflowapp.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsRoute(
    viewModel: UserDetailsViewModel,
    imageLoader: ImageLoader,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val followedUserIds by viewModel.followedUserIds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("User Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        when (val state = uiState) {
            UserDetailsUiState.Loading -> {
                LoadingScreen(modifier = modifier.then(Modifier.padding(paddingValues)))
            }

            is UserDetailsUiState.Success -> {
                UserDetailsScreen(
                    user = state.user,
                    isFollowed = state.user.id in followedUserIds,
                    onFollowClick = viewModel::onFollowClick,
                    imageLoader = imageLoader,
                    modifier = modifier.then(Modifier.padding(paddingValues))
                )
            }

            is UserDetailsUiState.Error -> {
                ErrorStateView(
                    title = "Error",
                    message = state.message,
                    onRetry = onBack
                )
            }
        }

    }

}
