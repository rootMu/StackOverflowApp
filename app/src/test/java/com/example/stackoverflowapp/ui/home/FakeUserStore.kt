package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.storage.UserStore

class FakeUserStore(initialIds: Set<Int> = emptySet()): UserStore {
    private var followedUserIds = initialIds

    override fun getFollowedUserIds(): Set<Int> = followedUserIds

    override fun setFollowedUserIds(ids: Set<Int>) {
        followedUserIds = ids
    }
}