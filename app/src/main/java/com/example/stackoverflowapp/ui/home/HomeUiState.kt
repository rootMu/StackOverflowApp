package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.domain.model.User

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(
        val users: List<User>,
        val followedUserIds: Set<Int> = emptySet(),
        val isRefreshing: Boolean = false
    ) : HomeUiState

    /**
     * Used when the request succeeded but no users are returned.
     */
    data object Empty : HomeUiState

    data class Error(
        val message: String
    ) : HomeUiState
}

internal fun List<User>.toHomeUiState(followedUserIds: Set<Int>): HomeUiState =
    if (isEmpty()) HomeUiState.Empty else HomeUiState.Success(
        users = this,
        followedUserIds = followedUserIds,
        isRefreshing = false
    )