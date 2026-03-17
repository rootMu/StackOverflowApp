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
        fetchUserDetails()
    }

    fun onFollowClick() {
        toggleFollowAsync(userId)
    }

    private fun fetchUserDetails() {
        _uiState.value = UserDetailsUiState.Loading

        viewModelScope.launch {

            userRepository.fetchUsersById(userId)
                .onSuccess { _uiState.value = UserDetailsUiState.Success(it) }
                .onFailure {
                    _uiState.value = UserDetailsUiState.Error("Unable to fetch User by id:$userId")
                }
        }
    }
}