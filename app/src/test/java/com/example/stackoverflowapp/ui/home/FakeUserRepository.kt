package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.repo.UserRepository
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

    override suspend fun refreshUsers(): Result<List<User>> {
        fetchCallCount++
        delay(10)
        return result
    }
}