package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.domain.model.User
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `empty list maps to Empty state`() {
        val state = emptyList<User>().toHomeUiState(emptySet())
        assertTrue(state is HomeUiState.Empty)
    }

    @Test
    fun `non empty list maps to Success state`() {
        val state = listOf(User(1, "Jeff", 1, null))
            .toHomeUiState(emptySet())
        assertTrue(state is HomeUiState.Success)
    }
}