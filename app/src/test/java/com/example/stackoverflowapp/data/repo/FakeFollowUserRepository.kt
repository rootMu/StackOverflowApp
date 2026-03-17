package com.example.stackoverflowapp.data.repo

import com.example.stackoverflowapp.data.storage.UserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFollowUserRepository(
    private val userStore: UserStore
) : FollowedUsersRepository {

    private val _followedUserIds = MutableStateFlow(userStore.getFollowedUserIds())
    override val followedUserIds: StateFlow<Set<Int>> = _followedUserIds.asStateFlow()

    override suspend fun toggleFollow(userId: Int) {
        val current = userStore.getFollowedUserIds().toMutableSet()
        if (!current.add(userId)) {
            current.remove(userId)
        }
        userStore.setFollowedUserIds(current)
        _followedUserIds.value = current
    }

}
