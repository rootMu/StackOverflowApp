package com.example.stackoverflowapp.domain.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable

/**
 * Helper function to create a [User] for testing purposes.
 * Placed in the debug source set to be shared between test and androidTest.
 */
fun createTestUser(
    id: Int = 1,
    name: String = "Test User",
    reputation: Int = 100,
    imageUrl: String? = null,
    location: String? = "London",
    website: String? = "https://example.com"
) = User(
    id = id,
    displayName = name,
    reputation = reputation,
    profileImageUrl = imageUrl,
    location = location,
    websiteUrl = website
)

/**
 * Reusable test wrapper to provide Shared Transition scopes.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionTestContext(
    content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit
) {
    SharedTransitionLayout {
        AnimatedContent(targetState = true, label = "test") { _ ->
            content(this@SharedTransitionLayout, this@AnimatedContent)
        }
    }
}
