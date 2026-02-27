package com.example.stackoverflowapp.ui.home

import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import com.example.stackoverflowapp.MainDispatcherRule
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserStore
import com.example.stackoverflowapp.domain.model.User
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init state transitions correctly`() = runTest {
        val users = listOf(createUser(1))
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo)

        runCurrent()

        assertEquals(HomeUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertSuccessState(viewModel.uiState.value) {
            assertEquals(users, it.users)
        }
    }

    @Test
    fun `init handles empty and error cases`() = runTest {
        val emptyRepo = FakeUserRepository(Result.success(emptyList()))
        assertEquals(
            HomeUiState.Empty,
            createViewModel(emptyRepo).apply { advanceUntilIdle() }.uiState.value
        )

        val errorRepo = FakeUserRepository(Result.failure(Exception("Net error")))
        val state = createViewModel(errorRepo).apply { advanceUntilIdle() }.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("Net error", (state as HomeUiState.Error).message)
    }

    @Test
    fun `init loads followed ids from store`() = runTest {
        val initialIds = setOf(1, 2)
        val viewModel = createViewModel(
            FakeUserRepository(Result.success(emptyList())),
            FakeUserStore(initialIds)
        )
        advanceUntilIdle()
        assertEquals(initialIds, viewModel.followedUserIds)
    }

    @Test
    fun `rapid toggling results in consistent state`() = runTest {
        val viewModel = createViewModel(FakeUserRepository(Result.success(emptyList())))
        advanceUntilIdle()

        repeat(7) { viewModel.toggleFollow(1) }
        advanceUntilIdle()

        assertTrue(
            "Odd number of toggles should result in followed",
            1 in viewModel.followedUserIds
        )
    }

    @Test
    fun `toggleFollow updates state and persists correctly`() = runTest {
        val store = FakeUserStore()
        val (viewModel, _) = initAndGetSuccess(listOf(createUser(1)), store)

        verifyToggle(viewModel, store, id = 1, shouldBeFollowed = true)
        verifyToggle(viewModel, store, id = 1, shouldBeFollowed = false)
    }

    @Test
    fun `refresh failure preserves data`() = runTest {
        val users = listOf(createUser(1))
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo).apply { advanceUntilIdle() }

        repo.setResult(Result.failure(Exception("Fail")))
        viewModel.refresh()
        advanceUntilIdle()

        assertSuccessState(viewModel.uiState.value) {
            assertEquals(users, it.users)
            assertFalse(it.isRefreshing)
        }
    }

    @Test
    fun `searchQuery filters users list correctly`() = runTest {
        val user1 = createUser(1, "Jeff Atwood")
        val user2 = createUser(2, "Joel Spolsky")
        val repo = FakeUserRepository(Result.success(listOf(user1, user2)))
        val viewModel = createViewModel(repo)

        backgroundCollect(viewModel.filteredUsers)
        advanceUntilIdle()

        withMutableSnapshot {
            viewModel.onSearchQueryChange("Jeff")
        }

        runCurrent()

        assertEquals(1, viewModel.filteredUsers.value.size)
        assertEquals("Jeff Atwood", viewModel.filteredUsers.value[0].displayName)
    }

    @Test
    fun `filteredUsers reacts to search query case-insensitively`() = runTest {
        val user1 = createUser(id = 1, name = "Jeff Atwood")
        val user2 = createUser(id = 2, name = "Joel Spolsky")
        val (viewModel, _) = initAndGetSuccess(listOf(user1, user2))

        backgroundCollect(viewModel.filteredUsers)

        withMutableSnapshot {
            viewModel.onSearchQueryChange("jeff")
        }
        runCurrent()

        assertEquals(1, viewModel.filteredUsers.value.size)
        assertEquals("Jeff Atwood", viewModel.filteredUsers.value[0].displayName)
    }

    @Test
    fun `filteredUsers filters by favorites correctly`() = runTest {
        val user1 = createUser(id = 1, name = "Jeff")
        val user2 = createUser(id = 2, name = "Joel")
        val store = FakeUserStore(initialIds = setOf(2))
        val (viewModel, _) = initAndGetSuccess(listOf(user1, user2), store)

        viewModel.toggleFavoritesFilter()

        backgroundCollect(viewModel.filteredUsers)

        assertEquals(1, viewModel.filteredUsers.value.size)
        assertEquals(2, viewModel.filteredUsers.value[0].id)
        assertEquals("Joel", viewModel.filteredUsers.value[0].displayName)
    }

    @Test
    fun `filteredUsers sorts by reputation ascending and descending`() = runTest {
        val userLow = createUser(id = 1, name = "Low").copy(reputation = 10)
        val userHigh = createUser(id = 2, name = "High").copy(reputation = 1000)
        val (viewModel, _) = initAndGetSuccess(listOf(userLow, userHigh))

        backgroundCollect(viewModel.filteredUsers)
        runCurrent()

        assertEquals(1000, viewModel.filteredUsers.value[0].reputation)

        withMutableSnapshot {
            viewModel.onSortOrderChange(HomeViewModel.SortOrder.REPUTATION_ASC)
        }
        runCurrent()

        assertEquals(10, viewModel.filteredUsers.value[0].reputation)
    }

    @Test
    fun `filteredUsers sorts by name A-Z`() = runTest {
        val userB = createUser(id = 1, name = "Bob")
        val userA = createUser(id = 2, name = "Alice")
        val (viewModel, _) = initAndGetSuccess(listOf(userB, userA))

        viewModel.onSortOrderChange(HomeViewModel.SortOrder.NAME_ASC)

        backgroundCollect(viewModel.filteredUsers)

        assertEquals("Alice", viewModel.filteredUsers.value[0].displayName)
        assertEquals("Bob", viewModel.filteredUsers.value[1].displayName)
    }

    @Test
    fun `filteredUsers combines multiple filters correctly`() = runTest {
        val user1 = createUser(id = 1, name = "Apple").copy(reputation = 10)
        val user2 = createUser(id = 2, name = "April").copy(reputation = 500)
        val store = FakeUserStore(initialIds = setOf(1, 2))
        val (viewModel, _) = initAndGetSuccess(listOf(user1, user2), store)

        viewModel.onSearchQueryChange("Ap")
        viewModel.toggleFavoritesFilter()
        viewModel.onSortOrderChange(HomeViewModel.SortOrder.REPUTATION_DESC)

        backgroundCollect(viewModel.filteredUsers)

        assertEquals(2, viewModel.filteredUsers.value.size)
        assertEquals("April", viewModel.filteredUsers.value[0].displayName)
    }

    private fun TestScope.initAndGetSuccess(
        users: List<User> = emptyList(),
        store: UserStore = FakeUserStore()
    ): Pair<HomeViewModel, HomeUiState.Success> {
        val repo = FakeUserRepository(Result.success(users))
        val viewModel = createViewModel(repo, store)
        advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        return viewModel to state
    }

    private fun TestScope.verifyToggle(
        viewModel: HomeViewModel,
        store: UserStore,
        id: Int,
        shouldBeFollowed: Boolean
    ) {
        viewModel.toggleFollow(id)
        runCurrent()
        assertEquals(shouldBeFollowed, id in viewModel.followedUserIds)
        assertSuccessState(viewModel.uiState.value) {
            assertEquals(shouldBeFollowed, id in it.followedUserIds)
        }
        advanceUntilIdle()
        assertEquals(shouldBeFollowed, id in store.getFollowedUserIds())
    }

    private fun createViewModel(
        repo: UserRepository,
        store: UserStore = FakeUserStore()
    ) = HomeViewModel(repo, store)

    private fun createUser(id: Int, name: String = "User $id") =
        User(id = id, displayName = name, reputation = 100, profileImageUrl = null)

    private fun assertSuccessState(state: HomeUiState, block: (HomeUiState.Success) -> Unit) {
        assertTrue("Expected Success state but was $state", state is HomeUiState.Success)
        block(state as HomeUiState.Success)
    }

    private fun TestScope.backgroundCollect(flow: StateFlow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            flow.collect {}
        }
    }

}