package com.example.stackoverflowapp.data.repo

import kotlinx.coroutines.flow.StateFlow

interface FollowedUsersRepository {
    val followedUserIds: StateFlow<Set<Int>>
    suspend fun toggleFollow(userId: Int)
}