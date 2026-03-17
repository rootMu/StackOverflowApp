package com.example.stackoverflowapp.ui.details

import com.example.stackoverflowapp.domain.model.User

sealed interface UserDetailsUiState {
    data object Loading : UserDetailsUiState
    data class Success(val user: User): UserDetailsUiState
    data class Error(val message: String): UserDetailsUiState
}