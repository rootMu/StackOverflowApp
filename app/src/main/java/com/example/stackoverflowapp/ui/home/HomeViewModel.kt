package com.example.stackoverflowapp.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.main.FollowViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    followedUsersRepository: FollowedUsersRepository
) : FollowViewModel<HomeUiState>(HomeUiState.Loading, followedUsersRepository) {

    var sortOrder by mutableStateOf(SortOrder.REPUTATION_DESC)
        private set

    var showFavouritesOnly by mutableStateOf(false)
        private set

    fun onSortOrderChange(newOrder: SortOrder) {
        sortOrder = newOrder
    }

    fun toggleFavoritesFilter() {
        showFavouritesOnly = !showFavouritesOnly
    }

    var searchQuery by mutableStateOf("")
        private set

    val filteredUsers: StateFlow<List<User>> = combine(
        _uiState,
        snapshotFlow { searchQuery },
        snapshotFlow { sortOrder },
        snapshotFlow { showFavouritesOnly },
        followedUserIds
    ) { state, query, sort, favouritesOnly, followedIds ->
        if (state is HomeUiState.Success) {
            state.users
                .filter { it.displayName.contains(query, ignoreCase = true) }
                .filter { !favouritesOnly || it.id in followedIds }
                .sortedWith(getComparator(sort))
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getComparator(sort: SortOrder) = when (sort) {
        SortOrder.NAME_ASC -> compareBy<User> { it.displayName.lowercase() }
        SortOrder.REPUTATION_DESC -> compareByDescending { it.reputation }
        SortOrder.REPUTATION_ASC -> compareBy { it.reputation }
    }

    init {
        loadUsers()
        viewModelScope.launch {
            followedUserIds.collect { ids ->
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(followedUserIds = ids)
                }
            }
        }
    }

    fun onFollowClick(userId: Int) {
        toggleFollowAsync(userId)
    }

    fun loadUsers() {
        viewModelScope.launch {
            handleUserFetch { userRepository.fetchTopUsers() }
        }
    }

    fun refresh() {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = currentState.copy(isRefreshing = true)

        viewModelScope.launch {
            handleUserFetch { userRepository.refreshUsers() }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    private suspend fun handleUserFetch(fetcher: suspend () -> Result<List<User>>) {
        val result = fetcher()
        val currentFollowedIds = followedUserIds.value
        val currentState = _uiState.value

        _uiState.value = result.fold(
            onSuccess = { users ->
                if (users.isEmpty()) HomeUiState.Empty
                else HomeUiState.Success(users, currentFollowedIds, isRefreshing = false)
            },
            onFailure = { error ->
                if (currentState is HomeUiState.Success) {
                    currentState.copy(isRefreshing = false)
                } else {
                    HomeUiState.Error(error.message ?: "Unknown Error")
                }
            }
        )
    }

    enum class SortOrder {
        NAME_ASC, REPUTATION_DESC, REPUTATION_ASC
    }


}
