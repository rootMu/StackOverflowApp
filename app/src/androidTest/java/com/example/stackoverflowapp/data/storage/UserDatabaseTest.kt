package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.domain.model.User
import org.junit.Assert.assertEquals
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

    private fun createUser(id: Int, rep: Int) = User(id, "User $id", rep, null)

    @Test
    fun databaseMaintainsStrictReputationSorting() {
        val users = listOf(createUser(1, 10), createUser(2, 500), createUser(3, 100))
        db.insertUsers(users)

        val result = db.getAllUsers()

        assertEquals(500, result[0].reputation)
        assertEquals(100, result[1].reputation)
        assertEquals(10, result[2].reputation)
    }
}