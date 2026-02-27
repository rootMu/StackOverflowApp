package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.yield

class FakeUserRepository(
    private val result: Result<List<User>>
) : UserRepository {

    var fetchCallCount = 0
        private set

    override suspend fun fetchTopUsers(): Result<List<User>> {
        fetchCallCount++
        return result
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        yield()
        return Result.success(listOf(User(1, "Jeff", 100, null)))
    }
}