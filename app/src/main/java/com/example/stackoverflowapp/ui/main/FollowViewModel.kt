package com.example.stackoverflowapp.ui.main

import com.example.stackoverflowapp.data.repo.FollowedUsersRepository

abstract class FollowViewModel<S>(
    initialState: S,
    followedUsersRepository: FollowedUsersRepository
) : BaseViewModel<S>(initialState),
    Followable by FollowableImpl(followedUsersRepository) {

    protected fun toggleFollowAsync(userId: Int) {
        launch { toggleFollow(userId) }
    }
}