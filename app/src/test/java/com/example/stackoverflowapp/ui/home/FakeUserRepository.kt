package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User

class FakeUserRepository(
    private val result: Result<List<User>>
) : UserRepository {

    var fetchCallCount = 0
        private set

    override suspend fun fetchTopUsers(): Result<List<User>> {
        fetchCallCount++
        return result
    }
}