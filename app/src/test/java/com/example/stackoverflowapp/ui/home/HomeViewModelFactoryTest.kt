package com.example.stackoverflowapp.ui.home

import androidx.lifecycle.ViewModel
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserStore
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelFactoryTest {

    private val repository: UserRepository = FakeUserRepository(Result.success(emptyList()))
    private val store: UserStore = FakeUserStore()
    private val factory = HomeViewModelFactory(repository, store)


    @Test
    fun `create returns HomeViewModel when requested`() {
        val viewModel = factory.create(HomeViewModel::class.java)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(viewModel is HomeViewModel)
    }


    @Test
    fun `create throws IllegalArgumentException when unknown ViewModel requested`() {
        class UnknownViewModel : ViewModel()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnknownViewModel::class.java)
        }

        assertTrue(exception.message!!.contains("Unknown ViewModel class"))
    }
}