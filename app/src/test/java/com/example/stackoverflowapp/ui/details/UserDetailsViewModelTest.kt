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
        val repo = FakeUserRepository(Result.failure(Exception("Specific Error Message")))

        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore())
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UserDetailsUiState.Error)
        assertEquals("Specific Error Message", (state as UserDetailsUiState.Error).message)
    }

    @Test
    fun `retry after failure success updates to success`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Initial Fail")))
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore())
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UserDetailsUiState.Error)

        val user = createTestUser(id = 123)
        repo.setResult(Result.success(listOf(user)))

        viewModel.retry()
        assertEquals(UserDetailsUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UserDetailsUiState.Success)
    }

    @Test
    fun `onFollowClick updates followed state but not user content`() = runTest {
        val user = createTestUser(id = 123)
        val store = FakeUserStore()
        val followedRepo = FakeFollowUserRepository(store)
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = FakeUserRepository(Result.success(listOf(user))),
            followedUsersRepository = followedRepo
        )

        advanceUntilIdle()
        val initialState = viewModel.uiState.value as UserDetailsUiState.Success

        viewModel.onFollowClick()
        advanceUntilIdle()

        assertTrue(123 in viewModel.followedUserIds.value)
        val postFollowState = viewModel.uiState.value as UserDetailsUiState.Success
        assertEquals(initialState.user, postFollowState.user)
    }

    @Test
    fun `followed state is correct when user starts already followed`() = runTest {
        val user = createTestUser(id = 123)
        val store = FakeUserStore(initialIds = setOf(123))
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = FakeUserRepository(Result.success(listOf(user))),
            followedUsersRepository = FakeFollowUserRepository(store)
        )

        advanceUntilIdle()
        assertTrue(123 in viewModel.followedUserIds.value)
    }
}
