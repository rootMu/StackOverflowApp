package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.domain.model.BadgeCounts
import com.example.stackoverflowapp.domain.model.createTestUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDatabaseTest {

    private lateinit var db: UserDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("users.db")
        db = UserDatabase(context)
    }

    @Test
    fun databaseMaintainsStrictReputationSorting() {
        val users = listOf(
            createTestUser(id = 1, reputation = 10),
            createTestUser(id = 2, reputation = 500),
            createTestUser(id = 3, reputation = 100)
        )
        db.insertUsers(users)

        val result = db.getAllUsers()

        assertEquals(500, result[0].reputation)
        assertEquals(100, result[1].reputation)
        assertEquals(10, result[2].reputation)
    }

    @Test
    fun databaseSavesAndRetrievesBadges() {
        val badgeCounts = BadgeCounts(gold = 10, silver = 20, bronze = 30)
        val user = createTestUser(
            id = 123,
            name = "Badge User",
            reputation = 1000,
            badgeCounts = badgeCounts
        )
        db.insertUsers(listOf(user))

        val result = db.getUserById(123)
        assertEquals(badgeCounts, result?.badgeCounts)
    }

    @Test
    fun getUserById_returnsCorrectUser() {
        val user1 = createTestUser(id = 1, name = "User 1")
        val user2 = createTestUser(id = 2, name = "User 2")
        db.insertUsers(listOf(user1, user2))

        val result = db.getUserById(1)
        assertEquals(user1, result)
    }

    @Test
    fun getUserById_returnsNull_ifNotFound() {
        val result = db.getUserById(999)
        assertNull(result)
    }

    @Test
    fun databaseSavesAndRetrievesAboutMe() {
        val user = createTestUser(
            id = 456,
            name = "Bio User",
            aboutMe = "This is my profile biography."
        )
        db.insertUsers(listOf(user))

        val result = db.getUserById(456)
        assertEquals("This is my profile biography.", result?.aboutMe)
    }
}
