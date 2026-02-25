package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.ApiResult
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.domain.model.User

class UserRepositoryImpl(
    private val usersApi: StackOverflowUsersApi
): UserRepository {

    override suspend fun fetchTopUsers(): Result<List<User>> {
        return when (val result = usersApi.fetchTopUsers(page = 1, pageSize = 20)) {
            is ApiResult.Success -> {
                Result.success(result.data.items.map { it.toDomain() })
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
}

private fun UserDto.toDomain(): User {
    return User(
        id = userId,
        displayName = displayName,
        reputation = reputation,
        profileImageUrl = profileImageUrl
    )
}