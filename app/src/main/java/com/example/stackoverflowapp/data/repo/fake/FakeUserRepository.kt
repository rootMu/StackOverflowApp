package com.example.stackoverflowapp.data.repo.fake

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import kotlinx.coroutines.delay

class FakeUserRepository: UserRepository {
    override suspend fun fetchTopUsers(): Result<List<User>> =
        with(delay(400)) {
            Result.success(
                listOf(
                    User(1, "Jeff Atwood", 9001, "https://upload.wikimedia.org/wikipedia/commons/3/36/Long_Zheng%2C_Dan_Rigsby%2C_Jeff_Atwood_%282979598012%29.jpg"),
                    User(2, "Joel Spolsky", 8000, "https://upload.wikimedia.org/wikipedia/commons/8/81/Joel_Spolsky_2014-06-18_%28cropped%29.jpg"),
                    User(3, "Charlie Brown", 1, "https://static.wikia.nocookie.net/bstudios/images/9/9b/Charlie-brown.png/revision/latest?cb=20161220204511"),
                    User(4, "John Doe", 27, null),
                    User(5, "Abigail Sparks", 1001, null),
                    User(6, "Jake Warburton", 53, null),
                    User(7, "Harry Fisher", 1337, null),
                )
            )
        }
}