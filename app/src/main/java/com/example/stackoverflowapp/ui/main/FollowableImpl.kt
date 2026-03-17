package com.example.stackoverflowapp.ui.main

import com.example.stackoverflowapp.data.repo.FollowedUsersRepository

class FollowableImpl(
    private val repo: FollowedUsersRepository,
) : Followable {
    override val followedUserIds = repo.followedUserIds

    override suspend fun toggleFollow(userId: Int) {
        repo.toggleFollow(userId)
    }
}