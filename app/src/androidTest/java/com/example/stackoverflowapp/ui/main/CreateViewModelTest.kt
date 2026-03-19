package com.example.stackoverflowapp.ui.main

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.LocalAppContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class CreateViewModelTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class TestViewModel(val container: AppContainer) : ViewModel()
    private class OtherViewModel() : ViewModel()

    private val fakeContainer = object : AppContainer {
        override val userRepository: UserRepository get() = throw NotImplementedError()
        override val followedUsersRepository: FollowedUsersRepository get() = throw NotImplementedError()
        override val imageLoader: ImageLoader get() = throw NotImplementedError()
        override val userDatabase: UserDatabase get() = throw NotImplementedError()
    }

    @Test
    fun createViewModel_usesAppContainerFromCompositionLocal() {
        var capturedViewModel: TestViewModel? = null

        composeRule.setContent {
            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                capturedViewModel = createViewModel { container ->
                    TestViewModel(container)
                }
            }
        }

        assertNotNull("ViewModel should have been created", capturedViewModel)
        assertEquals("ViewModel should have received the provided container", fakeContainer, capturedViewModel?.container)
    }

    @Test
    fun createViewModel_withSameKey_returnsSameInstance() {
        var vm1: TestViewModel? = null
        var vm2: TestViewModel? = null
        val key = "shared_key"

        composeRule.setContent {
            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                vm1 = createViewModel(key = key) { container -> TestViewModel(container) }
                vm2 = createViewModel(key = key) { container -> TestViewModel(container) }
            }
        }

        assertEquals("Should return the same ViewModel instance for the same key", vm1, vm2)
    }

    @Test
    fun createViewModel_withDifferentKeys_returnsDifferentInstances() {
        var vm1: TestViewModel? = null
        var vm2: TestViewModel? = null

        composeRule.setContent {
            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                vm1 = createViewModel(key = "key1") { container -> TestViewModel(container) }
                vm2 = createViewModel(key = "key2") { container -> TestViewModel(container) }
            }
        }

        assertNotEquals("Should return different ViewModel instances for different keys", vm1, vm2)
    }

    @Test
    fun createViewModel_usesDefaultKeyBasedOnClassName() {
        var vm1: TestViewModel? = null
        var vm2: OtherViewModel? = null

        composeRule.setContent {
            CompositionLocalProvider(LocalAppContainer provides fakeContainer) {
                vm1 = createViewModel { container -> TestViewModel(container) }
                vm2 = createViewModel { OtherViewModel() }
            }
        }

        assertNotNull(vm1)
        assertNotNull(vm2)
        assertNotEquals("ViewModels of different types should have different default keys", vm1, vm2)
    }
}
