package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.ApiResult
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
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

    override suspend fun refreshUsers(): Result<List<User>> {
        userDatabase.clearAllUsers()
        return fetchUsersFromApi()
    }

    private suspend fun fetchUsersFromApi() =
        when (val result = usersApi.fetchTopUsers(page = 1, pageSize = 20)) {
            is ApiResult.Success -> {
                val domainUsers = result.data.items.map { it.toDomain() }

                userDatabase.insertUsers(domainUsers)

                Result.success(domainUsers)
            }

            is ApiResult.Error.Http -> {
                Result.failure(Exception("HTTP ${result.code}: ${result.message ?: "Request failed"}"))
            }

            is ApiResult.Error.EmptyBody -> {
                Result.failure(Exception("Empty response body"))
            }

            is ApiResult.Error.Network -> {
                Result.failure(Exception(result.message))
            }

            is ApiResult.Error.Parse -> {
                Result.failure(Exception(result.message))
            }
        }

}

private fun UserDto.toDomain(): User {
    return User(
        id = userId,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl
    )
}