package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.domain.model.User

interface UserRepository {
    suspend fun fetchTopUsers(page: Int = 1): Result<List<User>>
    suspend fun fetchUserDetails(userId: Int): Result<User>
    suspend fun refreshUsers(): Result<List<User>>
}
