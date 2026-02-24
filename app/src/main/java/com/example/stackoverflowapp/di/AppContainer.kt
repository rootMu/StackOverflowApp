package com.example.stackoverflowapp.di

import android.content.Context
import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.HttpUrlConnectionClient
import com.example.stackoverflowapp.data.repo.UserRepository
import com.example.stackoverflowapp.data.repo.fake.FakeUserRepository

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

class AppContainer(context: Context) {

    val userRepository: UserRepository = FakeUserRepository()

    val httpClient: HttpClient by lazy {
        HttpUrlConnectionClient()
    }

}