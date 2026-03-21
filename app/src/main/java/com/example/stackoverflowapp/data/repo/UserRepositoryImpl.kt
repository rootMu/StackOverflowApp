package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.api.BadgeCountsDto
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.api.UserDto
import com.example.stackoverflowapp.data.api.toResult
import com.example.stackoverflowapp.data.storage.UserLocalDataSource
import com.example.stackoverflowapp.domain.model.BadgeCounts
import com.example.stackoverflowapp.domain.model.User

class UserRepositoryImpl(
    private val usersApi: StackOverflowUsersApi,
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    override suspend fun fetchTopUsers(page: Int): Result<List<User>> {
        if (page == 1) {
            val localUsers = localDataSource.getAllUsers()
            if (localUsers.isNotEmpty()) {
                return Result.success(localUsers)
            }
        }
        return fetchUsersFromApi(page = page)
    }

    override suspend fun fetchUserDetails(userId: Int): Result<User> {
        val localUser = localDataSource.getUserById(userId)

        if (localUser != null && localUser.hasCompleteDetails()) {
            return Result.success(localUser)
        }

        return usersApi.fetchUserDetails(userId)
            .toResult()
            .mapCatching { dto ->
                val userDto = dto.items.firstOrNull() ?: throw Exception("User not found")
                userDto.toDomain().also { localDataSource.insertUsers(listOf(it)) }
            }
            .let { apiResult ->
                localUser?.takeIf { apiResult.isFailure }?.let(Result.Companion::success) ?: apiResult
            }
    }

    private fun User.hasCompleteDetails(): Boolean = !aboutMe.isNullOrBlank()

    override suspend fun refreshUsers(): Result<List<User>> {
        return fetchUsersFromApi(page = 1, clearCache = true)
    }

    private suspend fun fetchUsersFromApi(page: Int, clearCache: Boolean = false): Result<List<User>> {
        return usersApi.fetchTopUsers(page = page, pageSize = 20).toResult().map { response ->
            val domainUsers = response.items.map { it.toDomain() }
            if (clearCache) localDataSource.clearAllUsers()
            localDataSource.insertUsers(domainUsers)
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
