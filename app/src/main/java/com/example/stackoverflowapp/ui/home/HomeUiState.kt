package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.domain.model.User

data class HomeScreenState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val users: List<UserUiModel> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.REPUTATION_DESC,
    val showFavouritesOnly: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false
)

data class UserUiModel(
    val user: User,
    val isFollowed: Boolean
)

enum class SortOrder {
    NAME_ASC, REPUTATION_DESC, REPUTATION_ASC
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val users: List<User>,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val currentPage: Int = 1,
        val endReached: Boolean = false
    ) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

internal fun List<User>.toHomeUiState(): HomeUiState =
    if (isEmpty()) HomeUiState.Empty else HomeUiState.Success(
        users = this,
        isRefreshing = false
    )
