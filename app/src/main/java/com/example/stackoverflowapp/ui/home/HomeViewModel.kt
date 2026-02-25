package com.example.stackoverflowapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = userRepository.fetchTopUsers()

            _uiState.value = if (result.isSuccess) {
                val users = result.getOrDefault(emptyList())
                users.toHomeUiState()
            } else {
                HomeUiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    fun toggleFollow(userId: Int) {
        val current = _uiState.value
        if(current !is HomeUiState.Success) return

        val updatedFollowedIds = current.followedUserIds.toMutableSet().apply {
            if(!add(userId)) remove(userId)
        }

        _uiState.value = current.copy(followedUserIds = updatedFollowedIds)
    }


}