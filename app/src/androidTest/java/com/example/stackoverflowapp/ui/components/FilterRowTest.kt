package com.example.stackoverflowapp.ui.components

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.ui.home.SortDirection
import com.example.stackoverflowapp.ui.home.SortField
import com.example.stackoverflowapp.ui.home.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [FilterRow] component ensuring correct state reflection and callback execution.
 * Updated to support the new Dropdown and Direction Toggle UI.
 */
@RunWith(AndroidJUnit4::class)
class FilterRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun favoritesChip_reflectsSelectedState() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.Default,
                showFavouritesOnly = true,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Favorites").assertIsSelected()
    }

    @Test
    fun clickingFavorites_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.Default,
                showFavouritesOnly = false,
                onToggleFavorites = { clicked = true },
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Favorites").performClick()
        assertTrue("Callback should be invoked when favorites chip is clicked", clicked)
    }

    @Test
    fun sortField_displaysCorrectLabel() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder(SortField.REPUTATION, SortDirection.DESC),
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Popularity").assertExists()
    }

    @Test
    fun clickingSortField_opensDropdownAndAllowsSelection() {
        var capturedOrder: SortOrder? = null
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder(SortField.REPUTATION, SortDirection.DESC),
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = { capturedOrder = it }
            )
        }

        composeRule.onNodeWithText("Popularity").performClick()
        composeRule.onNodeWithText("Name").performClick()
        
        assertEquals(SortField.NAME, capturedOrder?.field)
        assertEquals(SortDirection.DESC, capturedOrder?.direction)
    }

    @Test
    fun clickingDirectionToggle_invokesCallbackWithToggledDirection() {
        var capturedOrder: SortOrder? = null
        val initialDirection = SortDirection.ASC
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder(SortField.NAME, initialDirection),
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = { capturedOrder = it }
            )
        }

        composeRule.onNodeWithContentDescription(initialDirection.contentDescription).performClick()
        
        assertEquals(SortField.NAME, capturedOrder?.field)
        assertEquals(SortDirection.DESC, capturedOrder?.direction)
    }
}
