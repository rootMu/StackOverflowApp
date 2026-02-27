package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserStore
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

    private fun createViewModel(
        repo: UserRepository,
        store: UserStore = FakeUserStore()
    ) = HomeViewModel(repo, store)

    @Test
    fun `init loads users and emits Success when repository returns non-empty list`() = runTest {
        val users = listOf(
            User(id = 1, displayName = "Jeff Atwood", reputation = 9001, profileImageUrl = null)
        )
        val repo = FakeUserRepository(Result.success(users))

        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(users, (state as HomeUiState.Success).users)
        assertEquals(1, repo.fetchCallCount)
    }

    @Test
    fun `init loads followed ids from store correctly`() = runTest {
        val initialIds = setOf(10, 20)
        val store = FakeUserStore(initialIds = initialIds)
        val repo = FakeUserRepository(Result.success(emptyList()))

        val viewModel = createViewModel(repo, store)
        advanceUntilIdle()

        assertEquals(
            "ViewModel should initialize with IDs from the store",
            initialIds,
            viewModel.followedUserIds
        )
    }

    @Test
    fun `init emits Empty when repository returns empty list`() = runTest {
        val repo = FakeUserRepository(Result.success(emptyList()))

        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        assertEquals(HomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `init emits Error when repository returns failure`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Network down")))

        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("Network down", (state as HomeUiState.Error).message)
    }

    @Test
    fun `init handles empty store by defaulting to empty set`() = runTest {
        val store = FakeUserStore(initialIds = emptySet())
        val viewModel = createViewModel(FakeUserRepository(Result.success(emptyList())), store)
        advanceUntilIdle()

        assertTrue("Memory state should be an empty set", viewModel.followedUserIds.isEmpty())
    }

    @Test
    fun `loadUsers retries and calls repository again`() = runTest {
        val users = listOf(
            User(id = 1, displayName = "Joel Spolsky", reputation = 8000, profileImageUrl = null)
        )
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals(2, repo.fetchCallCount)
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `onFollowToggle adds user id when not already followed`() = runTest {
        val users = listOf(User(1, "Jeff", 100, null))
        val repo = FakeUserRepository(Result.success(users))
        val store = FakeUserStore()
        val viewModel = createViewModel(repo, store)

        advanceUntilIdle()
        viewModel.toggleFollow(1)
        advanceUntilIdle()

        assertTrue(1 in viewModel.followedUserIds)
        assertTrue(1 in store.getFollowedUserIds())
    }

    @Test
    fun `onFollowToggle removes user id when already followed`() = runTest {
        val users = listOf(User(1, "Jeff", 100, null))
        val repo = FakeUserRepository(Result.success(users))

        val store = FakeUserStore(initialIds = setOf(1))
        val viewModel = createViewModel(repo, store)

        advanceUntilIdle()
        assertTrue("Pre-condition failed: ID 1 should be followed", 1 in viewModel.followedUserIds)
        viewModel.toggleFollow(1)
        advanceUntilIdle()

        assertFalse("ID 1 should be removed from ViewModel", 1 in viewModel.followedUserIds)
        assertFalse("ID 1 should be removed from Store", 1 in store.getFollowedUserIds())
    }

    @Test
    fun `onFollowToggle only updates followed ids and preserves users`() = runTest {
        val users = listOf(
            User(1, "Jeff", 100, null),
            User(2, "Joel", 200, null)
        )
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        val before = viewModel.uiState.value as HomeUiState.Success
        viewModel.toggleFollow(2)
        advanceUntilIdle()

        val after = viewModel.uiState.value as HomeUiState.Success
        assertEquals(before.users, after.users)
        assertTrue(2 in after.followedUserIds)
        assertFalse(1 in after.followedUserIds)
    }

    @Test
    fun `rapidly toggling the same user id results in correct final state`() = runTest {
        val userId = 1
        val viewModel = createViewModel(FakeUserRepository(Result.success(emptyList())))
        advanceUntilIdle()

        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        viewModel.toggleFollow(userId)
        advanceUntilIdle()

        assertTrue(
            "Final state should be followed after odd number of toggles",
            userId in viewModel.followedUserIds
        )
    }

    @Test
    fun `following multiple users persists all IDs in the store`() = runTest {
        val store = FakeUserStore()
        val viewModel = createViewModel(FakeUserRepository(Result.success(emptyList())), store)
        advanceUntilIdle()

        viewModel.toggleFollow(1)
        viewModel.toggleFollow(2)
        viewModel.toggleFollow(3)
        viewModel.toggleFollow(4)
        viewModel.toggleFollow(5)
        viewModel.toggleFollow(6)
        viewModel.toggleFollow(6)
        advanceUntilIdle()

        val expected = setOf(1, 2, 3, 4, 5)
        assertEquals("All IDs should be in memory", expected, viewModel.followedUserIds)
        assertEquals(
            "All IDs should be in the persistent store",
            expected,
            store.getFollowedUserIds()
        )
    }

    @Test
    fun `onFollowToggle does nothing when ui state is not Success`() = runTest {
        val repo = FakeUserRepository(Result.failure(RuntimeException("boom")))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        val before = viewModel.uiState.value
        assertTrue(before is HomeUiState.Error)
        viewModel.toggleFollow(1)
        advanceUntilIdle()

        val after = viewModel.uiState.value
        assertEquals(before, after)
    }
}