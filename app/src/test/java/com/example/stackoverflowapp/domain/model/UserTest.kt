package com.example.stackoverflowapp.domain.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {
    @Test
    fun `user holds expected values`() {
        val badgeCounts = BadgeCounts(1, 2, 3)
        val user = createTestUser(
            id = 1,
            name = "Jeff Atwood",
            reputation = 9001,
            imageUrl = null,
            badgeCounts = badgeCounts
        )
        assertEquals(1, user.id)
        assertEquals("Jeff Atwood", user.displayName)
        assertEquals(9001, user.reputation)
        assertNull(user.profileImageUrl)
        assertEquals(badgeCounts, user.badgeCounts)
    }
}
