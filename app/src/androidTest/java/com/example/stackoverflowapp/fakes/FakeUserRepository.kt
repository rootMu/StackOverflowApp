package com.example.stackoverflowapp.fakes

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.delay

/**
 * A fake implementation of [UserRepository] that simulates network latency and pagination.
 *
 * This fake allows for granular control over network responses:
 * 1. Simulates latency via [delay] to test loading states.
 * 2. Can "hold" loading indefinitely using [shouldHoldLoading].
 * 3. Supports page-specific results for pagination testing.
 *
 * @param result The default result returned for all pages unless overridden.
 */
class FakeUserRepository(private var result: Result<List<User>>) : UserRepository {

    /**
     * Total number of times any fetch method was called.
     */
    var fetchCallCount = 0
        private set

    /**
     * Total number of times [refreshUsers] was called.
     */
    var refreshCallCount = 0
        private set

    /**
     * An optional override for user details results.
     */
    var userDetailsResult: Result<User>? = null

    /**
     * If true, repository calls will suspend indefinitely to simulate a long loading state.
     * Useful for testing UI components like progress indicators.
     */
    var shouldHoldLoading: Boolean = false

    private val resultsByPage = mutableMapOf<Int, Result<List<User>>>()

    override suspend fun fetchTopUsers(page: Int): Result<List<User>> {
        handleLoadingSimulation()
        fetchCallCount++
        return resultsByPage[page] ?: result
    }

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        handleLoadingSimulation()
        fetchCallCount++

        return userDetailsResult ?: result.mapCatching { users ->
            users.firstOrNull { it.id == userId }
                ?: throw NoSuchElementException("User with id $userId not found in fake")
        }
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        handleLoadingSimulation()
        fetchCallCount++
        refreshCallCount++
        return resultsByPage[1] ?: result
    }

    /**
     * Helper to centralize loading simulation logic.
     */
    private suspend fun handleLoadingSimulation() {
        if (shouldHoldLoading) {
            delay(Long.MAX_VALUE)
        }
        delay(10)
    }
}