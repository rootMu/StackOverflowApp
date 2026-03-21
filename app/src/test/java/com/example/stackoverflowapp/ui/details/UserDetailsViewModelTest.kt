package com.example.stackoverflowapp.ui.details

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException
import com.example.stackoverflowapp.domain.model.createTestUser
import com.example.stackoverflowapp.fakes.FakeFollowUserRepository
import com.example.stackoverflowapp.fakes.FakeUserRepository
import com.example.stackoverflowapp.fakes.FakeUserStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val errorBus = ErrorBus()

    @Test
    fun `init state transitions correctly to success`() = runTest {
        val user = createTestUser(id = 123, name = "Test User")
        val repo = FakeUserRepository(Result.success(listOf(user)))
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore()),
            errorBus = errorBus
        )

        assertEquals(UserDetailsUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UserDetailsUiState.Success)
        assertEquals(user, (state as UserDetailsUiState.Success).user)
    }

    @Test
    fun `init reports error to ErrorBus on failure`() = runTest {
        val expectedError = AppError.Network.ServerError
        val repo = FakeUserRepository(Result.failure(AppErrorException(expectedError)))

        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore()),
            errorBus = errorBus
        )

        val busErrorJob = backgroundScope.launch {
            val error = errorBus.errors.first()
            assertEquals(expectedError, error)
        }

        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is UserDetailsUiState.Error)
        busErrorJob.cancel()
    }

    @Test
    fun `retry after failure success updates to success`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Initial Fail")))
        val viewModel = UserDetailsViewModel(
            userId = 123,
            userRepository = repo,
            followedUsersRepository = FakeFollowUserRepository(FakeUserStore()),
            errorBus = errorBus
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
            followedUsersRepository = followedRepo,
            errorBus = errorBus
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
            followedUsersRepository = FakeFollowUserRepository(store),
            errorBus = errorBus
        )

        advanceUntilIdle()
        assertTrue(123 in viewModel.followedUserIds.value)
    }
}
