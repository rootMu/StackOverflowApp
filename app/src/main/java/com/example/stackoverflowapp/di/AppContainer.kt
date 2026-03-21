package com.example.stackoverflowapp.di

import android.content.Context
import com.example.stackoverflowapp.data.api.StackOverflowUsersApiImpl
import com.example.stackoverflowapp.data.api.StackOverflowUsersApi
import com.example.stackoverflowapp.data.image.HttpImageLoader
import com.example.stackoverflowapp.data.image.ImageLoader
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.HttpUrlConnectionClient
import com.example.stackoverflowapp.data.parser.JsonUsersResponseParser
import com.example.stackoverflowapp.data.parser.UsersResponseParser
import com.example.stackoverflowapp.data.repo.DefaultFollowedUsersRepository
import com.example.stackoverflowapp.data.repo.FollowedUsersRepository
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.repo.UserRepositoryImpl
import com.example.stackoverflowapp.data.storage.SharedPrefsUserStore
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.data.storage.UserStore
import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.domain.GlobalExceptionHandler
import com.example.stackoverflowapp.ui.details.UserDetailsViewModel
import com.example.stackoverflowapp.ui.home.HomeViewModel

/**
 * Central application-level dependency container.
 *
 * This container acts as the "Composition Root" for manual dependency injection.
 * It manages the lifecycle of singletons and provides factory methods for ViewModels.
 */
interface AppContainer {
    val userRepository: UserRepository
    val followedUsersRepository: FollowedUsersRepository
    val imageLoader: ImageLoader
    val userDatabase: UserDatabase
    val errorBus: ErrorBus

    /** Creates a fresh instance of [HomeViewModel] with all required dependencies. */
    fun createHomeViewModel(): HomeViewModel

    /** Creates a [UserDetailsViewModel] for a specific user. */
    fun createUserDetailsViewModel(userId: Int): UserDetailsViewModel
}

class DefaultAppContainer(private val context: Context): AppContainer {

    override val errorBus: ErrorBus by lazy {
        ErrorBus().also {
            GlobalExceptionHandler.install(it)
        }
    }

    private val httpClient: HttpClient by lazy {
        HttpUrlConnectionClient()
    }

    private val usersParser: UsersResponseParser by lazy {
        JsonUsersResponseParser()
    }

    private val usersApi: StackOverflowUsersApi by lazy {
        StackOverflowUsersApiImpl(httpClient, usersParser)
    }

    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(usersApi, userDatabase)
    }

    override val imageLoader: ImageLoader by lazy {
        HttpImageLoader(httpClient)
    }

    override val followedUsersRepository: DefaultFollowedUsersRepository by lazy {
        DefaultFollowedUsersRepository(userStore)
    }

    private val userStore: UserStore by lazy {
        SharedPrefsUserStore(context)
    }

    override val userDatabase: UserDatabase by lazy {
        UserDatabase(context)
    }

    override fun createHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            userRepository = userRepository,
            followedUsersRepository = followedUsersRepository,
            errorBus = errorBus
        )
    }

    override fun createUserDetailsViewModel(userId: Int): UserDetailsViewModel {
        return UserDetailsViewModel(
            userId = userId,
            userRepository = userRepository,
            followedUsersRepository = followedUsersRepository,
            errorBus = errorBus
        )
    }
}
