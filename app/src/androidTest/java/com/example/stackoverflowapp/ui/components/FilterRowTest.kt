package com.example.stackoverflowapp.ui.components

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.ui.home.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [FilterRow] component ensuring correct state reflection and callback execution.
 */
@RunWith(AndroidJUnit4::class)
class FilterRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun favoritesChip_reflectsSelectedState() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = true,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Favorites").assertIsSelected()
    }

    @Test
    fun favoritesChip_reflectsUnselectedState() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Favorites").assertIsNotSelected()
    }

    @Test
    fun clickingFavorites_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = false,
                onToggleFavorites = { clicked = true },
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Favorites").performClick()
        assertTrue("Callback should be invoked when favorites chip is clicked", clicked)
    }

    @Test
    fun nameAscChip_isSelected_whenSortOrderIsNameAsc() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.NAME_ASC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Name A-Z").assertIsSelected()
    }

    @Test
    fun clickingNameAsc_invokesCallbackWithCorrectOrder() {
        var capturedOrder: SortOrder? = null
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = { capturedOrder = it }
            )
        }

        composeRule.onNodeWithText("Name A-Z").performClick()
        assertEquals(SortOrder.NAME_ASC, capturedOrder)
    }

    @Test
    fun popularityChip_showsDownArrow_whenReputationDesc() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Popularity ↓").assertIsSelected()
    }

    @Test
    fun popularityChip_showsUpArrow_whenReputationAsc() {
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_ASC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = {}
            )
        }

        composeRule.onNodeWithText("Popularity ↑").assertIsSelected()
    }

    @Test
    fun clickingPopularity_fromDesc_togglesToAsc() {
        var capturedOrder: SortOrder? = null
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_DESC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = { capturedOrder = it }
            )
        }

        composeRule.onNodeWithText("Popularity ↓").performClick()
        assertEquals("Should toggle to Ascending when clicked from Descending", SortOrder.REPUTATION_ASC, capturedOrder)
    }

    @Test
    fun clickingPopularity_fromAsc_togglesToDesc() {
        var capturedOrder: SortOrder? = null
        composeRule.setContent {
            FilterRow(
                sortOrder = SortOrder.REPUTATION_ASC,
                showFavouritesOnly = false,
                onToggleFavorites = {},
                onSortOrderChange = { capturedOrder = it }
            )
        }

        composeRule.onNodeWithText("Popularity ↑").performClick()
        assertEquals("Should toggle to Descending when clicked from Ascending", SortOrder.REPUTATION_DESC, capturedOrder)
    }
}
