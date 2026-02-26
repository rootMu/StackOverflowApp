package com.example.stackoverflowapp.ui.home

import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelFactoryTest {

    @Test
    fun `create returns HomeViewModel when requested class is HomeViewModel`() {
        val factory = HomeViewModelFactory(
            FakeUserRepository(Result.success(emptyList())),
            FakeUserStore()
        )

        val viewModel = factory.create(HomeViewModel::class.java)

        @Suppress("USELESS_IS_CHECK")
        assertTrue(viewModel is HomeViewModel)
    }
}