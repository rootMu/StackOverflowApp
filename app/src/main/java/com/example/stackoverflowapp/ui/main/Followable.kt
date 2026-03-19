package com.example.stackoverflowapp.ui.main

import kotlinx.coroutines.flow.StateFlow

interface Followable {
    val followedUserIds: StateFlow<Set<Int>>
    suspend fun toggleFollow(userId: Int)
}