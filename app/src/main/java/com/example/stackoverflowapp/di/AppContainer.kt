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
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.repo.UserRepositoryImpl
import com.example.stackoverflowapp.data.storage.SharedPrefsUserStore
import com.example.stackoverflowapp.data.storage.UserDatabase
import com.example.stackoverflowapp.data.storage.UserStore

/**
 * Central application-level dependency container.
 *
 * Creates and connects the main objects used by the app.
 * Keeps setup code in one place so shared dependencies are easy to manage.
 *
 * Dependency groups typically include:
 * - Networking: HTTP client and network configuration
 * - API: service interfaces / endpoint definitions
 * - Parser: serialization / deserialization components
 * - Repository: data access layer combining remote/local sources
 * - Persistence: local storage (database, preferences, caches)
 *
 */

interface AppContainer {
    val userRepository: UserRepository
    val imageLoader: ImageLoader
    val userStore: UserStore
    val userDatabase: UserDatabase
}

class DefaultAppContainer(private val context: Context): AppContainer {

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

    override val userStore: UserStore by lazy {
        SharedPrefsUserStore(context)
    }

    override val userDatabase: UserDatabase by lazy {
        UserDatabase(context)
    }

}