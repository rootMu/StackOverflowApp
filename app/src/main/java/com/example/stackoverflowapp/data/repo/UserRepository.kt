package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.domain.model.User

interface UserRepository {
    suspend fun fetchTopUsers(): Result<List<User>>
}