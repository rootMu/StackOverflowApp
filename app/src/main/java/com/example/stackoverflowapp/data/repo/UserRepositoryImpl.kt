package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.toResult
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.domain.model.User

class UserRepositoryImpl(
    private val usersApi: StackOverflowUsersApi,
    private val userDatabase: UserDatabase
) : UserRepository {

    override suspend fun fetchTopUsers(): Result<List<User>> {
        val localUsers = userDatabase.getAllUsers()
        if (localUsers.isNotEmpty()) {
            return Result.success(localUsers)
        }
        return fetchUsersFromApi()
    }

    override suspend fun fetchUsersById(userId: Int): Result<User> {
        val localUser = userDatabase.getUserById(userId)
        if (localUser != null) {
            return Result.success(localUser)
        }
        // In a real app, we might call fetchUserById(userId) here if not found in DB
        return Result.failure(Exception("User with id: $userId not found"))
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        return fetchUsersFromApi(clearCache = true)
    }

    private suspend fun fetchUsersFromApi(clearCache: Boolean = false): Result<List<User>> {
        return usersApi.fetchTopUsers(page = 1, pageSize = 20).toResult { response ->
            val domainUsers = response.items.map { it.toDomain() }
            if (clearCache) userDatabase.clearAllUsers()
            userDatabase.insertUsers(domainUsers)
            domainUsers
        }
    }
}

private fun UserDto.toDomain(): User {
    return User(
        id = userId,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl,
        location = location,
        websiteUrl = websiteUrl
    )
}
