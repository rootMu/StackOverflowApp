package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.delay

class FakeUserRepository(private var result: Result<List<User>>) : UserRepository {
    var fetchCallCount = 0
    fun setResult(newResult: Result<List<User>>) {
        result = newResult
    }

    override suspend fun fetchTopUsers(): Result<List<User>> {
        fetchCallCount++
        delay(10)
        return result
    }

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        fetchCallCount++
        delay(10)
        return result.mapCatching { it.first { user -> user.id == userId } }
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        fetchCallCount++
        delay(10)
        return result
    }
}
