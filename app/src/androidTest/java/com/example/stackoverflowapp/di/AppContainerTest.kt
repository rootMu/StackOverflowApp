package com.example.stackoverflowapp.di

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContainerTest {

    @Test
    fun defaultAppContainer_instantiatesAllDependencies() {
        val container = DefaultAppContainer(ApplicationProvider.getApplicationContext())

        assertNotNull(container.userRepository)
        assertNotNull(container.followedUsersRepository)
        assertNotNull(container.imageLoader)
        assertNotNull(container.userDatabase)
    }

    @Test
    fun defaultAppContainer_providesSingletons() {
        val container = DefaultAppContainer(ApplicationProvider.getApplicationContext())

        val repo1 = container.userRepository
        val repo2 = container.userRepository

        assertSame(repo1, repo2)
    }
}
