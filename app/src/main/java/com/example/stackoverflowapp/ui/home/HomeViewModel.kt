package com.example.stackoverflowapp.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserStore
import kotlinx.coroutines.Dispatchers
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

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = userRepository.fetchTopUsers()

            _uiState.value = if (result.isSuccess) {
                val users = result.getOrDefault(emptyList())
                users.toHomeUiState(followedUserIds)
            } else {
                HomeUiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    fun toggleFollow(userId: Int) {
        val newIds = followedUserIds.toMutableSet().apply {
            if (!add(userId)) remove(userId)
        }.toSet()

        followedUserIds = newIds

        viewModelScope.launch {
            userStore.setFollowedUserIds(newIds)
            updateUiState()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            userRepository.refreshUsers()
            isRefreshing = false
        }
    }

    private fun updateUiState() {
        if (_uiState.value !is HomeUiState.Success) return
        val current = _uiState.value as HomeUiState.Success
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.value = current.copy(followedUserIds = followedUserIds)
        }
    }


}