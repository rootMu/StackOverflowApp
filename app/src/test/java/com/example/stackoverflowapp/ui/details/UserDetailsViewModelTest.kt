package com.example.stackoverflowapp.ui.details

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.data.repo.FakeFollowUserRepository
import com.example.stackoverflowapp.data.repo.FakeUserRepository
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.ui.home.FakeUserStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init state transitions correctly to success`() = runTest {
        val user = createTestUser(id = 123, name = "Test User")
        val repo = FakeUserRepository(Result.success(listOf(user)))
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore())
        )

        assertEquals(UserDetailsUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UserDetailsUiState.Success)
        assertEquals(user, (state as UserDetailsUiState.Success).user)
    }

    @Test
    fun `init state transitions correctly to error`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Not found")))

        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore())
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UserDetailsUiState.Error)
    }

    @Test
    fun `onFollowClick updates followed state`() = runTest {
        val user = createTestUser(id = 123)
        val store = FakeUserStore()
        val followedRepo = FakeFollowUserRepository(store)
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = FakeUserRepository(Result.success(listOf(user))),
            followedUsersRepository = followedRepo
        )

        advanceUntilIdle()
        
        viewModel.onFollowClick()
        advanceUntilIdle()

        assertTrue(123 in viewModel.followedUserIds.value)
        assertTrue(123 in store.getFollowedUserIds())

        viewModel.onFollowClick()
        advanceUntilIdle()

        assertTrue(123 !in viewModel.followedUserIds.value)
        assertTrue(123 !in store.getFollowedUserIds())
    }
}
