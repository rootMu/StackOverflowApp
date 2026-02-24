package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads users and emits Success when repository returns non-empty list`() = runTest {
        val users = listOf(
            User(id = 1, displayName = "Jeff Atwood", reputation = 9001, profileImageUrl = null)
        )
        val repo = FakeTestUserRepository(Result.success(users))

        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(users, (state as HomeUiState.Success).users)
        assertEquals(1, repo.fetchCallCount)
    }

    @Test
    fun `init emits Empty when repository returns empty list`() = runTest {
        val repo = FakeTestUserRepository(Result.success(emptyList()))

        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        assertEquals(HomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `init emits Error when repository returns failure`() = runTest {
        val repo = FakeTestUserRepository(Result.failure(Exception("Network down")))

        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("Network down", (state as HomeUiState.Error).message)
    }

    @Test
    fun `loadUsers retries and calls repository again`() = runTest {
        val users = listOf(
            User(id = 1, displayName = "Joel Spolsky", reputation = 8000, profileImageUrl = null)
        )
        val repo = FakeTestUserRepository(Result.success(users))
        val viewModel = HomeViewModel(repo)
        advanceUntilIdle()

        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals(2, repo.fetchCallCount)
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }
}