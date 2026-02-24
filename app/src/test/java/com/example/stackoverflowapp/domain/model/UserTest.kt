package com.example.stackoverflowapp.domain.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {
    @Test
    fun `user holds expected values`() {
        val user = User(1, "Jeff Atwood", 9001, null)
        assertEquals(1, user.id)
        assertEquals("Jeff Atwood", user.displayName)
        assertEquals(9001, user.reputation)
        assertNull(user.profileImageUrl)
    }
}