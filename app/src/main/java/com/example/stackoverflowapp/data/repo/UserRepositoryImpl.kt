package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.BadgeCountsDto
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.toResult
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.domain.model.BadgeCounts
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

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        val localUser = userDatabase.getUserById(userId)

        if (localUser != null && !localUser.aboutMe.isNullOrBlank()) {
            return Result.success(localUser)
        }

        return usersApi.fetchUserDetails(userId)
            .toResult()
            .mapCatching { dto ->
                val userDto = dto.items.firstOrNull() ?: throw Exception("User not found")
                userDto.toDomain().also { userDatabase.insertUsers(listOf(it)) }
            }
            .let { apiResult ->
                localUser?.takeIf { apiResult.isFailure }?.let(Result.Companion::success) ?: apiResult
            }
    }

    override suspend fun refreshUsers(): Result<List<User>> {
        return fetchUsersFromApi(clearCache = true)
    }

    private suspend fun fetchUsersFromApi(clearCache: Boolean = false): Result<List<User>> {
        return usersApi.fetchTopUsers(page = 1, pageSize = 20).toResult().map { response ->
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
        badgeCounts = badgeCounts?.toDomain(),
        location = location,
        websiteUrl = websiteUrl,
        aboutMe = aboutMe
    )
}

private fun BadgeCountsDto.toDomain(): BadgeCounts {
    return BadgeCounts(
        bronze = bronze,
        silver = silver,
        gold = gold
    )
}
