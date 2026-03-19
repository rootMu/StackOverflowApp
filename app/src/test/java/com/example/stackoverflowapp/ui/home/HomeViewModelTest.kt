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
    fun `searchQuery filters users list correctly`() = runTest {
        val user1 = createTestUser(1, "Jeff Atwood")
        val user2 = createTestUser(2, "Joel Spolsky")
        val repo = FakeUserRepository(Result.success(listOf(user1, user2)))
        val viewModel = createViewModel(repo)
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Jeff")
        runCurrent()

        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals("Jeff Atwood", viewModel.screenState.value.users[0].user.displayName)
    }

    @Test
    fun `screenState filters by favorites correctly`() = runTest {
        val user1 = createTestUser(id = 1, name = "Jeff")
        val user2 = createTestUser(id = 2, name = "Joel")
        val store = FakeUserStore(initialIds = setOf(2))
        val viewModel = createViewModel(
            FakeUserRepository(Result.success(listOf(user1, user2))),
            FakeFollowUserRepository(store)
        )
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        viewModel.toggleFavoritesFilter()
        runCurrent()

        assertEquals(1, viewModel.screenState.value.users.size)
        assertEquals(2, viewModel.screenState.value.users[0].user.id)
    }

    @Test
    fun `screenState sorts by reputation ascending and descending`() = runTest {
        val userLow = createTestUser(id = 1, name = "Low").copy(reputation = 10)
        val userHigh = createTestUser(id = 2, name = "High").copy(reputation = 1000)
        val viewModel =
            createViewModel(FakeUserRepository(Result.success(listOf(userLow, userHigh))))
        backgroundCollect(viewModel.screenState)
        advanceUntilIdle()

        assertEquals(1000, viewModel.screenState.value.users[0].user.reputation)

        viewModel.onSortOrderChange(SortOrder.REPUTATION_ASC)
        runCurrent()

        assertEquals(10, viewModel.screenState.value.users[0].user.reputation)
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
