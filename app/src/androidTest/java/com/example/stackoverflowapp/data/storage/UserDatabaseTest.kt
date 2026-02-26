package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.domain.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDatabaseTest {

    private lateinit var db: UserDatabase
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        context.deleteDatabase("users.db")
        db = UserDatabase(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetAllUsers_returnsUsersSortedByReputation() {
        val user1 = User(1, "Low Rep", 100, "url1")
        val user2 = User(2, "High Rep", 5000, "url2")
        val user3 = User(3, "Mid Rep", 1000, "url3")
        val users = listOf(user1, user2, user3)

        db.insertUsers(users)
        val result = db.getAllUsers()

        assertEquals(3, result.size)
        assertEquals("High Rep", result[0].displayName)
        assertEquals("Mid Rep", result[1].displayName)
        assertEquals("Low Rep", result[2].displayName)
    }

    @Test
    fun insertWithConflict_updatesExistingUser() {
        val initialUser = User(1, "Old Name", 100, "old_url")
        db.insertUsers(listOf(initialUser))

        val updatedUser = User(1, "New Name", 200, "new_url")
        db.insertUsers(listOf(updatedUser))

        val result = db.getAllUsers()

        assertEquals(1, result.size)
        assertEquals("New Name", result[0].displayName)
        assertEquals(200, result[0].reputation)
    }

    @Test
    fun clearAllUsers_removesAllData() {
        db.insertUsers(listOf(User(1, "Test", 10, null)))

        db.clearAllUsers()
        val result = db.getAllUsers()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllUsers_returnsEmptyList_whenDatabaseIsEmpty() {
        val result = db.getAllUsers()
        assertTrue(result.isEmpty())
    }
}