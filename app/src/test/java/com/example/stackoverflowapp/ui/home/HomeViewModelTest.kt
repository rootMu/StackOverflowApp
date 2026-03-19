package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.data.repo.FakeFollowUserRepository
import com.example.stackoverflowapp.data.repo.FakeUserRepository
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.createTestUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init state transitions correctly`() = runTest {
        val users = listOf(createTestUser(1))
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo)

        backgroundCollect(viewModel.screenState)
        runCurrent()

        assertTrue(viewModel.screenState.value.isLoading)

        advanceUntilIdle()
        val state = viewModel.screenState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.users.size)
        assertEquals(users[0], state.users[0].user)
    }

    @Test
    fun `init handles empty and error cases`() = runTest {
        val emptyRepo = FakeUserRepository(Result.success(emptyList()))
        val viewModelEmpty = createViewModel(emptyRepo)
        backgroundCollect(viewModelEmpty.screenState)
        advanceUntilIdle()
        assertTrue(viewModelEmpty.screenState.value.users.isEmpty())

        val errorRepo = FakeUserRepository(Result.failure(Exception("Net error")))
        val viewModelError = createViewModel(errorRepo)
        backgroundCollect(viewModelError.screenState)
        advanceUntilIdle()
        assertEquals("Net error", viewModelError.screenState.value.error)
    }

    @Test
    fun `init loads followed ids`() = runTest {
        val initialIds = setOf(1, 2)
        val user1 = createTestUser(1)
        val viewModel = createViewModel(
            FakeUserRepository(Result.success(listOf(user1))),
            FakeFollowUserRepository(FakeUserStore(initialIds))
        )
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()
        assertTrue(viewModel.screenState.value.users.first { it.user.id == 1 }.isFollowed)
    }

    @Test
    fun `toggleFollow updates state correctly`() = runTest {
        val user1 = createTestUser(1)
        val viewModel = createViewModel(FakeUserRepository(Result.success(listOf(user1))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onFollowClick(1)
        runCurrent()
        assertTrue(viewModel.screenState.value.users.first { it.user.id == 1 }.isFollowed)

        viewModel.onFollowClick(1)
        runCurrent()
        assertFalse(viewModel.screenState.value.users.first { it.user.id == 1 }.isFollowed)
    }

    @Test
    fun `refresh success updates users and clears refreshing flag`() = runTest {
        val initialUsers = listOf(createTestUser(1))
        val repo = FakeUserRepository(Result.success(initialUsers))
        val viewModel = createViewModel(repo)
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        val freshUsers = listOf(createTestUser(1), createTestUser(2))
        repo.setResult(Result.success(freshUsers))
        
        viewModel.refresh()
        assertTrue(viewModel.screenState.value.isRefreshing)
        
        advanceUntilIdle()
        
        val state = viewModel.screenState.value
        assertFalse(state.isRefreshing)
        assertEquals(2, state.users.size)
    }

    @Test
    fun `refresh ignored when not in success state`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Initial Error")))
        val viewModel = createViewModel(repo)
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        assertTrue(viewModel.screenState.value.error != null)
        
        repo.setResult(Result.success(listOf(createTestUser(1))))
        viewModel.refresh()
        
        runCurrent()
        assertFalse(viewModel.screenState.value.isRefreshing)
        assertEquals(0, repo.refreshCallCount)
    }

    @Test
    fun `refresh failure preserves data`() = runTest {
        val users = listOf(createTestUser(1))
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo)
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        repo.setResult(Result.failure(Exception("Fail")))
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertEquals(1, state.users.size)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
    }

    @Test
    fun `search query blank shows all users`() = runTest {
        val users = listOf(createTestUser(1, "A"), createTestUser(2, "B"))
        val viewModel = createViewModel(FakeUserRepository(Result.success(users)))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("A")
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)

        viewModel.onSearchQueryChange("")
        runCurrent()
        assertEquals(2, viewModel.screenState.value.users.size)
    }

    @Test
    fun `search change does not lose current sort order`() = runTest {
        val userLow = createTestUser(id = 1, name = "Alice").copy(reputation = 10)
        val userHigh = createTestUser(id = 2, name = "Bob").copy(reputation = 1000)
        val viewModel = createViewModel(FakeUserRepository(Result.success(listOf(userLow, userHigh))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onSortOrderChange(SortOrder.REPUTATION_ASC)
        runCurrent()

        viewModel.onSearchQueryChange("Bob")
        runCurrent()

        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals(SortOrder.REPUTATION_ASC, viewModel.screenState.value.sortOrder)
    }

    @Test
    fun `following a user while favourites-only is enabled causes them to appear immediately`() = runTest {
        val user1 = createTestUser(id = 1, name = "Jeff")
        val viewModel = createViewModel(FakeUserRepository(Result.success(listOf(user1))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.toggleFavoritesFilter()
        runCurrent()
        assertTrue(viewModel.screenState.value.users.isEmpty())

        viewModel.onFollowClick(1)
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals(1, viewModel.screenState.value.users[0].user.id)
    }

    @Test
    fun `unfollowing the only favourite while favourites-only is enabled removes them immediately`() = runTest {
        val user1 = createTestUser(id = 1, name = "Jeff")
        val store = FakeUserStore(initialIds = setOf(1))
        val viewModel = createViewModel(
            FakeUserRepository(Result.success(listOf(user1))),
            FakeFollowUserRepository(store)
        )
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.toggleFavoritesFilter()
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)

        viewModel.onFollowClick(1)
        runCurrent()
        assertTrue(viewModel.screenState.value.users.isEmpty())
    }

    @Test
    fun `search, favourites and sorting all recompute correctly after follow toggle`() = runTest {
        val u1 = createTestUser(1, "Alice").copy(reputation = 10)
        val u2 = createTestUser(2, "Alex").copy(reputation = 20)
        val u3 = createTestUser(3, "Bob").copy(reputation = 30)
        
        val viewModel = createViewModel(FakeUserRepository(Result.success(listOf(u1, u2, u3))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Al") // Alice, Alex
        viewModel.onSortOrderChange(SortOrder.REPUTATION_DESC) // Alex (20), Alice (10)
        viewModel.toggleFavoritesFilter() // None
        runCurrent()
        assertTrue(viewModel.screenState.value.users.isEmpty())

        viewModel.onFollowClick(1) // Alice
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals(1, viewModel.screenState.value.users[0].user.id)

        viewModel.onFollowClick(2) // Alice, Alex
        runCurrent()
        assertEquals(2, viewModel.screenState.value.users.size)
        assertEquals(2, viewModel.screenState.value.users[0].user.id) // Alex (20) first
    }

    @Test
    fun `sort change does not lose current search filter`() = runTest {
        val user1 = createTestUser(id = 1, name = "Alice").copy(reputation = 100)
        val user2 = createTestUser(id = 2, name = "Bob").copy(reputation = 200)
        val viewModel = createViewModel(FakeUserRepository(Result.success(listOf(user1, user2))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Alice")
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)

        viewModel.onSortOrderChange(SortOrder.REPUTATION_ASC)
        runCurrent()
        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals("Alice", viewModel.screenState.value.users[0].user.displayName)
    }

    @Test
    fun `error on initial load followed by retry success updates to success`() = runTest {
        val repo = FakeUserRepository(Result.failure(Exception("Network Error")))
        val viewModel = createViewModel(repo)
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        assertTrue(viewModel.screenState.value.error != null)

        repo.setResult(Result.success(listOf(createTestUser(1))))
        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertNull(state.error)
        assertEquals(1, state.users.size)
    }

    private fun createViewModel(
        repo: UserRepository,
        followedUsersRepository: FollowedUsersRepository = FakeFollowUserRepository(FakeUserStore())
    ) = HomeViewModel(repo, followedUsersRepository)

    private fun TestScope.backgroundCollect(flow: StateFlow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            flow.collect {}
        }
    }
}
