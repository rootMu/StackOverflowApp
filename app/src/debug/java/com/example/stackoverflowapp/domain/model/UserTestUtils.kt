package com.example.stackoverflowapp.domain.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.stackoverflowapp.ui.transitions.LocalAnimatedVisibilityScope
import com.example.stackoverflowapp.ui.transitions.LocalSharedTransitionScope

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
    website: String? = "https://example.com",
    badgeCounts: BadgeCounts? = BadgeCounts(1, 2, 3),
    aboutMe: String? = null,
    creationDate: Long? = null,
    lastModifiedDate: Long? = null
) = User(
    id = id,
    displayName = name,
    reputation = reputation,
    profileImageUrl = imageUrl,
    location = location,
    websiteUrl = website,
    badgeCounts = badgeCounts,
    aboutMe = aboutMe,
    creationDate = creationDate,
    lastModifiedDate = lastModifiedDate
)

/**
 * Reusable test wrapper to provide Shared Transition scopes.
 * Transitions are disabled by default to ensure UI stability during assertions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionTestContext(
    content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit
) {
    SharedTransitionLayout {
        AnimatedContent(
            targetState = true,
            label = "test",
            transitionSpec = { EnterTransition.None togetherWith ExitTransition.None }
        ) { targetState ->
            if (targetState) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@AnimatedContent
                ) {
                    content(this@SharedTransitionLayout, this@AnimatedContent)
                }
            }
        }
    }
}
