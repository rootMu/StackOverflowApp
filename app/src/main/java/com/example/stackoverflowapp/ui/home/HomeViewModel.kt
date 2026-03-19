package com.example.stackoverflowapp.ui.home

import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.ui.main.FollowViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    followedUsersRepository: FollowedUsersRepository
) : FollowViewModel<HomeUiState>(HomeUiState.Loading, followedUsersRepository) {

    private val _searchQuery = MutableStateFlow("")

    private val _sortOrder = MutableStateFlow(SortOrder.REPUTATION_DESC)

    private val _showFavouritesOnly = MutableStateFlow(false)

    val screenState: StateFlow<HomeScreenState> = combine(
        uiState,
        _searchQuery,
        _sortOrder,
        _showFavouritesOnly,
        followedUserIds
    ) { state, query, sort, favouritesOnly, followedIds ->
        HomeScreenState(
            isLoading = state is HomeUiState.Loading,
            isRefreshing = (state as? HomeUiState.Success)?.isRefreshing ?: false,
            users = if (state is HomeUiState.Success) {
                state.users
                    .filter { it.displayName.contains(query, ignoreCase = true) }
                    .filter { !favouritesOnly || it.id in followedIds }
                    .sortedWith(getComparator(sort))
                    .map { UserUiModel(it, it.id in followedIds) }
            } else {
                emptyList()
            },
            searchQuery = query,
            sortOrder = sort,
            showFavouritesOnly = favouritesOnly,
            error = (state as? HomeUiState.Error)?.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState(isLoading = true)
    )

    private fun getComparator(sort: SortOrder) = when (sort) {
        SortOrder.NAME_ASC -> compareBy<User> { it.displayName.lowercase() }
        SortOrder.REPUTATION_DESC -> compareByDescending { it.reputation }
        SortOrder.REPUTATION_ASC -> compareBy { it.reputation }
    }

    init {
        loadUsers()
    }

    fun onSortOrderChange(newOrder: SortOrder) {
        _sortOrder.value = newOrder
    }

    fun toggleFavoritesFilter() {
        _showFavouritesOnly.update { !it }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
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

    private suspend fun handleUserFetch(fetcher: suspend () -> Result<List<User>>) {
        val result = fetcher()
        val currentState = _uiState.value

        _uiState.value = result.fold(
            onSuccess = { users ->
                if (users.isEmpty()) HomeUiState.Empty
                else HomeUiState.Success(users, isRefreshing = false)
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
}
