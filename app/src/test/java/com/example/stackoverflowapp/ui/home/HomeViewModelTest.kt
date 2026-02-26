package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun `onFollowToggle adds user id when not already followed`() = runTest {
        val users = listOf(User(1, "Jeff", 100, null))
        val repo = FakeTestUserRepository(Result.success(users))
        val viewModel = HomeViewModel(repo)

        // ensure initial load completes
        advanceUntilIdle()

        val before = viewModel.uiState.value
        assertTrue(before is HomeUiState.Success)

        viewModel.toggleFollow(1)

        val after = viewModel.uiState.value as HomeUiState.Success
        assertTrue(1 in after.followedUserIds)
    }

    @Test
    fun `onFollowToggle removes user id when already followed`() = runTest {
        val users = listOf(User(1, "Jeff", 100, null))
        val repo = FakeTestUserRepository(Result.success(users))
        val viewModel = HomeViewModel(repo)

        advanceUntilIdle()

        viewModel.toggleFollow(1) // follow
        viewModel.toggleFollow(1) // unfollow

        val state = viewModel.uiState.value as HomeUiState.Success
        assertFalse(1 in state.followedUserIds)
    }

    @Test
    fun `onFollowToggle only updates followed ids and preserves users`() = runTest {
        val users = listOf(
            User(1, "Jeff", 100, null),
            User(2, "Joel", 200, null)
        )
        val repo = FakeTestUserRepository(Result.success(users))
        val viewModel = HomeViewModel(repo)

        advanceUntilIdle()

        val before = viewModel.uiState.value as HomeUiState.Success

        viewModel.toggleFollow(2)

        val after = viewModel.uiState.value as HomeUiState.Success
        assertEquals(before.users, after.users)
        assertTrue(2 in after.followedUserIds)
        assertFalse(1 in after.followedUserIds)
    }

    @Test
    fun `onFollowToggle does nothing when ui state is not Success`() = runTest {
        val repo = FakeTestUserRepository(Result.failure(RuntimeException("boom")))
        val viewModel = HomeViewModel(repo)

        advanceUntilIdle()

        val before = viewModel.uiState.value
        assertTrue(before is HomeUiState.Error)

        viewModel.toggleFollow(1)

        val after = viewModel.uiState.value
        assertEquals(before, after)
    }
}