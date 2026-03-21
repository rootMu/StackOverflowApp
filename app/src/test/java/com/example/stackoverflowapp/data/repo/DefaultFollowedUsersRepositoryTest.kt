package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.fakes.FakeUserStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultFollowedUsersRepositoryTest {

    @Test
    fun `followedUserIds reflects initial state from store`() = runTest {
        val store = FakeUserStore()
        store.setFollowedUserIds(setOf(1, 2))
        val repository = DefaultFollowedUsersRepository(store)

        assertEquals(setOf(1, 2), repository.followedUserIds.value)
    }

    @Test
    fun `toggleFollow adds id if not present`() = runTest {
        val store = FakeUserStore()
        val repository = DefaultFollowedUsersRepository(store)

        repository.toggleFollow(123)

        assertTrue(123 in repository.followedUserIds.value)
        assertTrue(123 in store.getFollowedUserIds())
    }

    @Test
    fun `toggleFollow removes id if already present`() = runTest {
        val store = FakeUserStore()
        store.setFollowedUserIds(setOf(123))
        val repository = DefaultFollowedUsersRepository(store)

        repository.toggleFollow(123)

        assertTrue(123 !in repository.followedUserIds.value)
        assertTrue(123 !in store.getFollowedUserIds())
    }

    @Test
    fun `toggleFollow preserves other followed ids`() = runTest {
        val store = FakeUserStore()
        store.setFollowedUserIds(setOf(1, 2))
        val repository = DefaultFollowedUsersRepository(store)

        repository.toggleFollow(3)

        assertEquals(setOf(1, 2, 3), repository.followedUserIds.value)

        repository.toggleFollow(1)
        assertEquals(setOf(2, 3), repository.followedUserIds.value)
    }

    @Test
    fun `multiple toggles on different ids produce expected final set`() = runTest {
        val store = FakeUserStore()
        val repository = DefaultFollowedUsersRepository(store)

        repository.toggleFollow(1)
        repository.toggleFollow(2)
        repository.toggleFollow(1)
        repository.toggleFollow(3)

        assertEquals(setOf(2, 3), repository.followedUserIds.value)
    }
}
