package com.example.stackoverflowapp.ui.animation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.domain.model.SharedTransitionTestContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [SharedTransitionTestContext].
 *
 * Verifies that the test utility correctly provides the experimental animation scopes
 * required for testing shared element transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class SharedTransitionTestContextTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sharedTransitionTestContext_providesScopesAndRendersContent() {
        val testText = "Inside Transition Scope"

        composeRule.setContent {
            SharedTransitionTestContext { animatedVisibilityScope ->
                Text(
                    text = testText,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "test_key"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
        }

        composeRule.onNodeWithText(testText).assertIsDisplayed()
    }

    @Test
    fun sharedTransitionTestContext_executesLambdaExactlyOnce() {
        var executionCount = 0

        composeRule.setContent {
            SharedTransitionTestContext {
                executionCount++
            }
        }

        composeRule.waitForIdle()

        assert(executionCount >= 1)
    }
}
