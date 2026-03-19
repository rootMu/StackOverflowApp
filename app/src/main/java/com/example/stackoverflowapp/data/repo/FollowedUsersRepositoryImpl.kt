package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.storage.UserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultFollowedUsersRepository(
    private val userStore: UserStore
) : FollowedUsersRepository {

    private val _followedUserIds = MutableStateFlow(userStore.getFollowedUserIds())
    override val followedUserIds: StateFlow<Set<Int>> = _followedUserIds.asStateFlow()

    override suspend fun toggleFollow(userId: Int) {
        val newIds = _followedUserIds.value.toMutableSet().apply {
            if (!add(userId)) remove(userId)
        }.toSet()

        _followedUserIds.value = newIds
        userStore.setFollowedUserIds(newIds)
    }
}