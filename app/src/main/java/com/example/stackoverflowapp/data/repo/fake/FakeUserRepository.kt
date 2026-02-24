package com.example.stackoverflowapp.data.repo.fake

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.delay

class FakeUserRepository: UserRepository {
    override suspend fun fetchTopUsers(): Result<List<User>> =
        with(delay(400)) {
            Result.success(
                listOf(
                    User(1, "Jeff Atwood", 9001, null),
                    User(1, "Joel Spolsky", 8000, null)
                )
            )
        }
}