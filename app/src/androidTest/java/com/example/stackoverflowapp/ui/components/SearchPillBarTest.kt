package com.example.stackoverflowapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchPillBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchPillBar_displaysPlaceholder_whenQueryIsEmpty() {
        composeTestRule.setContent {
            SearchPillBar(query = "", onQueryChange = {})
        }

        composeTestRule.onNodeWithText("Search users...").assertIsDisplayed()
    }

    @Test
    fun searchPillBar_invokesCallback_onTextChange() {
        var capturedQuery = ""
        composeTestRule.setContent {
            SearchPillBar(
                query = "",
                onQueryChange = { capturedQuery = it }
            )
        }

        composeTestRule.onNodeWithText("Search users...").performTextInput("Jeff")

        Assert.assertEquals("Jeff", capturedQuery)
    }

    @Test
    fun searchPillBar_showsQuery_whenProvided() {
        composeTestRule.setContent {
            SearchPillBar(query = "Jeff", onQueryChange = {})
        }

        composeTestRule.onNodeWithText("Jeff").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search users...").assertDoesNotExist()
    }
}