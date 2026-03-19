package com.example.stackoverflowapp.ui.details

import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.ui.main.FollowViewModel
import kotlinx.coroutines.launch

class UserDetailsViewModel(
    private val userId: Int,
    private val userRepository: UserRepository,
    followedUsersRepository: FollowedUsersRepository
) : FollowViewModel<UserDetailsUiState>(UserDetailsUiState.Loading, followedUsersRepository) {

    init {
        loadUserDetails()
    }

    fun onFollowClick() {
        toggleFollowAsync(userId)
    }

    fun retry() {
        loadUserDetails()
    }

    private fun loadUserDetails() {
        _uiState.value = UserDetailsUiState.Loading

        viewModelScope.launch {
            userRepository.fetchUserDetails(userId)
                .onSuccess { _uiState.value = UserDetailsUiState.Success(it) }
                .onFailure { error ->
                    _uiState.value = UserDetailsUiState.Error(error.message ?: "Unknown Error")
                }
        }
    }
}
