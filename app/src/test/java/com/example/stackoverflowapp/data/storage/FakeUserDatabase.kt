package com.example.stackoverflowapp.data.storage

import com.example.stackoverflowapp.domain.model.User

class FakeUserDatabase : UserDatabase(
    context = null
) {

    private var users = mutableListOf<User>()

    override suspend fun getAllUsers(): List<User> = users

    override suspend fun getUserById(userId: Int): User? = users.find { it.id == userId }

    override suspend fun insertUsers(users: List<User>) {
        val newIds = users.map { it.id }.toSet()
        this.users.removeAll { it.id in newIds }
        this.users.addAll(users)
        this.users.sortByDescending { it.reputation }
    }

    override suspend fun clearAllUsers() {
        users.clear()
    }

}
