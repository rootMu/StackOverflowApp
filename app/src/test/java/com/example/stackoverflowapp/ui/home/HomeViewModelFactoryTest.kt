package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.domain.model.User
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelFactoryTest {

    private val fakeRepo = object : UserRepository {
        override suspend fun fetchTopUsers(): Result<List<User>> = Result.success(emptyList())
    }

    @Test
    fun `create returns HomeViewModel when requested class is HomeViewModel`() {
        val factory = HomeViewModelFactory(fakeRepo)

        val viewModel = factory.create(HomeViewModel::class.java)

        @Suppress("USELESS_IS_CHECK")
        assertTrue(viewModel is HomeViewModel)
    }
}