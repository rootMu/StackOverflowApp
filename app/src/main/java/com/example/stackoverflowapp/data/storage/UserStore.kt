package com.example.stackoverflowapp.data.storage

interface UserStore {
    fun getFollowedUserIds(): Set<Int>
    fun setFollowedUserIds(ids: Set<Int>)
}


