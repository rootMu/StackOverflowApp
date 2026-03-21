package com.example.stackoverflowapp.ui.main

import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.domain.ErrorBus

abstract class FollowViewModel<S>(
    initialState: S,
    followedUsersRepository: FollowedUsersRepository,
    errorBus: ErrorBus? = null
) : BaseViewModel<S>(initialState, errorBus),
    Followable by FollowableImpl(followedUsersRepository) {

    protected fun toggleFollowAsync(userId: Int) {
        launch(handleErrorsGlobally = true) {
            toggleFollow(userId)
        }
    }
}
