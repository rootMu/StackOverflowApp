package com.example.stackoverflowapp.ui.details

import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.ui.main.FollowViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the User Details screen.
 *
 * Responsible for fetching extended user profile information and managing the
 * following status for a specific user.
 *
 * @param userId The ID of the user to display.
 * @param userRepository Repository for user data operations.
 * @param followedUsersRepository Repository for tracking followed status.
 * @param errorBus Central bus for reporting global application errors.
 */
class UserDetailsViewModel(
    private val userId: Int,
    private val userRepository: UserRepository,
    followedUsersRepository: FollowedUsersRepository,
    errorBus: ErrorBus
) : FollowViewModel<UserDetailsUiState>(
    initialState = UserDetailsUiState.Loading,
    followedUsersRepository = followedUsersRepository,
    errorBus = errorBus
) {

    init {
        loadUserDetails()
    }

    /**
     * Toggles the follow status for the current user.
     * Errors are handled globally via the [ErrorBus] through the base class implementation.
     */
    fun onFollowClick() {
        toggleFollowAsync(userId)
    }

    /**
     * Re-attempts to fetch user details.
     */
    fun retry() {
        loadUserDetails()
    }

    private fun loadUserDetails() {
        _uiState.value = UserDetailsUiState.Loading

        viewModelScope.launch {
            userRepository.fetchUserDetails(userId)
                .onSuccess { user ->
                    _uiState.value = UserDetailsUiState.Success(user)
                }
                .onFailure { throwable ->
                    postError(throwable)
                    _uiState.value = UserDetailsUiState.Error(
                        message = throwable.message ?: "Unknown Error"
                    )
                }
        }
    }
}
