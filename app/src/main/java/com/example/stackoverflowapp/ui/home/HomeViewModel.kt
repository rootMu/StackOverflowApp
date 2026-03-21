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

    private val _sortOrder = MutableStateFlow(
        SortOrder.Default
    )

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
            isLoadingMore = (state as? HomeUiState.Success)?.isLoadingMore ?: false,
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
            error = (state as? HomeUiState.Error)?.message,
            endReached = (state as? HomeUiState.Success)?.endReached ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState(isLoading = true)
    )

    private fun getComparator(sort: SortOrder): Comparator<User> {
        val baseComparator = when (sort.field) {
            SortField.NAME -> compareBy<User> { it.displayName.lowercase() }
            SortField.REPUTATION -> compareBy<User> { it.reputation }
            SortField.CREATION -> compareBy<User> { it.creationDate ?: 0L }
            SortField.MODIFIED -> compareBy<User> { it.lastModifiedDate ?: 0L }
        }

        return when (sort.direction) {
            SortDirection.ASC -> baseComparator
            SortDirection.DESC -> baseComparator.reversed()
        }
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
            handleUserFetch(page = 1)
        }
    }

    fun loadMoreUsers() {
        val currentState = uiState.value as? HomeUiState.Success ?: return
        if (currentState.isLoadingMore || currentState.endReached) return

        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            handleUserFetch(page = currentState.currentPage + 1)
        }
    }

    fun refresh() {
        val currentState = uiState.value as? HomeUiState.Success ?: return
        _uiState.value = currentState.copy(isRefreshing = true)

        viewModelScope.launch {
            val result = userRepository.refreshUsers()
            _uiState.value = result.fold(
                onSuccess = { users ->
                    HomeUiState.Success(
                        users = users,
                        isRefreshing = false,
                        currentPage = 1,
                        endReached = users.isEmpty()
                    )
                },
                onFailure = {
                    currentState.copy(isRefreshing = false)
                }
            )
        }
    }

    private suspend fun handleUserFetch(page: Int) {
        val result = userRepository.fetchTopUsers(page)
        val currentState = uiState.value

        _uiState.value = result.fold(
            onSuccess = { newUsers ->
                if (page == 1) {
                    if (newUsers.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(newUsers, currentPage = 1)
                    }
                } else {
                    val currentSuccess = currentState as? HomeUiState.Success
                    if (currentSuccess != null) {
                        val mergedUsers = (currentSuccess.users + newUsers).distinctBy { it.id }
                        currentSuccess.copy(
                            users = mergedUsers,
                            isLoadingMore = false,
                            currentPage = page,
                            endReached = newUsers.isEmpty()
                        )
                    } else {
                        HomeUiState.Success(newUsers, currentPage = page)
                    }
                }
            },
            onFailure = { error ->
                when (currentState) {
                    is HomeUiState.Success -> currentState.copy(
                        isLoadingMore = false,
                        isRefreshing = false
                    )

                    else -> HomeUiState.Error(error.message ?: "Unknown Error")
                }
            }
        )
    }
}