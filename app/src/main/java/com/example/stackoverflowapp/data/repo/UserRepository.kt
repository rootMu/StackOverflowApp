package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.domain.model.User

interface UserRepository {
    suspend fun fetchTopUsers(): Result<List<User>>
    suspend fun fetchUsersById(userId: Int): Result<User>
    suspend fun refreshUsers(): Result<List<User>>
}