package com.example.stackoverflowapp.data.storage

import com.example.stackoverflowapp.domain.model.User

interface UserLocalDataSource {
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(userId: Int): User?
    suspend fun insertUsers(users: List<User>)
    suspend fun clearAllUsers()
}
