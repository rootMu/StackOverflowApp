package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.fakes.FakeFollowUserRepository
import com.example.stackoverflowapp.fakes.FakeUserRepository
import com.example.stackoverflowapp.fakes.FakeUserStore
import com.example.stackoverflowapp.ui.main.GenericViewModelFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelFactoryTest {

    private val repository: UserRepository = FakeUserRepository(Result.success(emptyList()))
    private val followedUsersRepository: FollowedUsersRepository =
        FakeFollowUserRepository(FakeUserStore())
    private val factory =
        GenericViewModelFactory { HomeViewModel(repository, followedUsersRepository) }


    @Test
    fun `create returns HomeViewModel when requested`() {
        val viewModel = factory.create(HomeViewModel::class.java)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(viewModel is HomeViewModel)
    }

}
