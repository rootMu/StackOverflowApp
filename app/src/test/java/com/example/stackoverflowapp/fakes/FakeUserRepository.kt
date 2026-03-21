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

    private val resultsByPage = mutableMapOf<Int, Result<List<User>>>()
    
    fun setResult(newResult: Result<List<User>>) {
        result = newResult
    }

    fun setResultForPage(page: Int, newResult: Result<List<User>>) {
        resultsByPage[page] = newResult
    }

    override suspend fun fetchTopUsers(page: Int): Result<List<User>> {
        fetchCallCount++
        delay(10)
        return resultsByPage[page] ?: result
    }

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        fetchCallCount++
        delay(10)
        return result.mapCatching { users -> 
            users.first { it.id == userId } 
        }
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        fetchCallCount++
        refreshCallCount++
        delay(10)
        return resultsByPage[1] ?: result
    }
}
