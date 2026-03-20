package com.example.stackoverflowapp.fakes

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.delay

/**
 * A fake implementation of [UserRepository] that simulates network latency.
 *
 * The [delay] in each method ensures that coroutines suspend, allowing tests
 * to verify intermediate states like 'Loading' or 'Refreshing'.
 */
class FakeUserRepository(private var result: Result<List<User>>) : UserRepository {
    var fetchCallCount = 0
        private set
    var refreshCallCount = 0
        private set

    var userDetailsResult: Result<User>? = null
    
    /**
     * If true, repository calls will suspend indefinitely to simulate a long loading state.
     */
    var shouldHoldLoading: Boolean = false

    override suspend fun fetchTopUsers(): Result<List<User>> {
        if (shouldHoldLoading) delay(Long.MAX_VALUE)
        fetchCallCount++
        delay(10)
        return result
    }

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        if (shouldHoldLoading) delay(Long.MAX_VALUE)
        fetchCallCount++
        delay(10)
        
        return userDetailsResult ?: result.mapCatching { users ->
            users.first { it.id == userId }
        }
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        if (shouldHoldLoading) delay(Long.MAX_VALUE)
        fetchCallCount++
        refreshCallCount++
        delay(10)
        return result
    }
}
