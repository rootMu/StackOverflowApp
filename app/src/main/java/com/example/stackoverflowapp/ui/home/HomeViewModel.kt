package com.example.stackoverflowapp.ui.home

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserStore
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val userStore: UserStore
) : ViewModel() {

    var sortOrder by mutableStateOf(SortOrder.REPUTATION_DESC)
        private set

    var showFavoritesOnly by mutableStateOf(false)
        private set

    fun onSortOrderChange(newOrder: SortOrder) { sortOrder = newOrder }
    fun toggleFavoritesFilter() { showFavoritesOnly = !showFavoritesOnly }

    var searchQuery by mutableStateOf("")
        private set
    var followedUserIds by mutableStateOf(userStore.getFollowedUserIds())
        private set
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val filteredUsers by derivedStateOf {
        val state = uiState.value
        if (state is HomeUiState.Success) {
            state.users
                .filter { it.displayName.contains(searchQuery, ignoreCase = true) }
                .filter { if (showFavoritesOnly) it.id in followedUserIds else true }
                .sortedWith(
                    when (sortOrder) {
                    SortOrder.NAME_ASC -> compareBy { it.displayName.lowercase() }
                    SortOrder.REPUTATION_DESC -> compareByDescending { it.reputation }
                    SortOrder.REPUTATION_ASC -> compareBy { it.reputation }
                })
        } else {
            emptyList()
        }
    }

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            handleUserFetch { userRepository.fetchTopUsers() }
        }
    }

    fun toggleFollow(userId: Int) {
        val newIds = followedUserIds.toMutableSet().apply {
            if (!add(userId)) remove(userId)
        }.toSet()

        followedUserIds = newIds

        viewModelScope.launch {
            userStore.setFollowedUserIds(newIds)
        }

        updateUiState()
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

        val currentState = _uiState.value

        _uiState.value = result.fold(
            onSuccess = { users ->
                if (users.isEmpty()) HomeUiState.Empty
                else HomeUiState.Success(users, followedUserIds, isRefreshing = false)
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

    private fun updateUiState() {
        if (_uiState.value !is HomeUiState.Success) return
        val current = _uiState.value as HomeUiState.Success
        _uiState.value = current.copy(followedUserIds = followedUserIds)
    }

    enum class SortOrder {
        NAME_ASC, REPUTATION_DESC, REPUTATION_ASC
    }


}