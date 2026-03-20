package com.example.stackoverflowapp.ui.home

import com.example.stackoverflowapp.domain.model.User
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `empty list maps to Empty state`() {
        val state = emptyList<User>().toHomeUiState()
        assertTrue(state is HomeUiState.Empty)
    }

    @Test
    fun `non empty list maps to Success state`() {
        val state = listOf(createTestUser(id = 1, name = "Jeff"))
            .toHomeUiState()
        assertTrue(state is HomeUiState.Success)
    }
}
