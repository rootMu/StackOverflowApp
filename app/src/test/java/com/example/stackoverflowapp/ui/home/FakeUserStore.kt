package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.storage.UserStore

class FakeUserStore(initialIds: Set<Int> = emptySet()) : UserStore {
    private var ids = initialIds
    override fun getFollowedUserIds() = ids
    override fun setFollowedUserIds(ids: Set<Int>) { this@FakeUserStore.ids = ids }
}