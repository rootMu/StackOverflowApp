package com.example.stackoverflowapp.data.storage

import com.example.stackoverflowapp.domain.model.User

class FakeUserDatabase : UserDatabase(
    context = null
) {

    private var users = mutableListOf<User>()

    override fun getAllUsers(): List<User> = users

    override fun insertUsers(users: List<User>) {
        val newIds = users.map { it.id }.toSet()
        this.users.removeAll { it.id in newIds }
        this.users.addAll(users)
        this.users.sortByDescending { it.reputation }
    }

    override fun clearAllUsers() {
        users.clear()
    }

}