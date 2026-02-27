package com.example.stackoverflowapp.ui.home

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

    var followedUserIds by mutableStateOf(userStore.getFollowedUserIds())
        private set
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        handleUserFetch { userRepository.fetchTopUsers() }
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

    private fun handleUserFetch(block: suspend () -> Result<List<User>>) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = block()

            _uiState.value = result.fold(
                onSuccess = { users ->
                    if (users.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        users.toHomeUiState(followedUserIds)
                    }
                },
                onFailure = {
                    val current = _uiState.value
                    if (current is HomeUiState.Success) {
                        current.copy(isRefreshing = false)
                    } else {
                        HomeUiState.Error(it.message ?: "Something went wrong")
                    }
                }

            )
        }
    }

    private fun updateUiState() {
        if (_uiState.value !is HomeUiState.Success) return
        val current = _uiState.value as HomeUiState.Success
        viewModelScope.launch {
            _uiState.value = current.copy(followedUserIds = followedUserIds)
        }
    }


}