package com.example.stackoverflowapp.fakes

import android.graphics.Bitmap
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.ui.details.UserDetailsViewModel
import com.example.stackoverflowapp.ui.home.HomeViewModel

/**
 * A fake implementation of [AppContainer] for use in instrumentation tests.
 * Allows providing custom fakes for repositories and services.
 */
class FakeAppContainer(
    override val userRepository: UserRepository = FakeUserRepository(Result.success(emptyList())),
    override val followedUsersRepository: FollowedUsersRepository = FakeFollowUserRepository(FakeUserStore()),
    override val imageLoader: ImageLoader = object : ImageLoader {
        override suspend fun loadBitmap(url: String): Bitmap? = null
        override fun getCachedBitmap(url: String): Bitmap? = null
    },
    override val userDatabase: UserDatabase = FakeUserDatabase(),
    override val errorBus: ErrorBus = ErrorBus()
) : AppContainer {

    override fun createHomeViewModel(): HomeViewModel {
        return HomeViewModel(userRepository, followedUsersRepository, errorBus)
    }

    override fun createUserDetailsViewModel(userId: Int): UserDetailsViewModel {
        return UserDetailsViewModel(userId, userRepository, followedUsersRepository, errorBus)
    }
}

/**
 * Minimal fake for [UserDatabase] to avoid real SQLite operations in tests that don't need them.
 */
private class FakeUserDatabase : UserDatabase(null) {
    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {}
    override fun onUpgrade(db: android.database.sqlite.SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}
