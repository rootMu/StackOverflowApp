package com.example.stackoverflowapp.data.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stackoverflowapp.domain.model.BadgeCounts
import com.example.stackoverflowapp.domain.model.createTestUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun databaseSavesAndRetrievesNewSortFields() = runBlocking {
        val user = createTestUser(
            id = 1,
            creationDate = 1000L,
            lastModifiedDate = 2000L
        )
        db.insertUsers(listOf(user))

        val result = db.getUserById(1)
        assertEquals(1000L, result?.creationDate)
        assertEquals(2000L, result?.lastModifiedDate)
    }

    @Test
    fun databaseSavesAndRetrievesBadges() = runBlocking {
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
    fun getUserById_returnsCorrectUser() = runBlocking {
        val user1 = createTestUser(id = 1, name = "User 1")
        val user2 = createTestUser(id = 2, name = "User 2")
        db.insertUsers(listOf(user1, user2))

        val result = db.getUserById(1)
        assertEquals(user1, result)
    }

    @Test
    fun getUserById_returnsNull_ifNotFound() = runBlocking {
        val result = db.getUserById(999)
        assertNull(result)
    }

    @Test
    fun databaseSavesAndRetrievesAboutMe() = runBlocking {
        val user = createTestUser(
            id = 456,
            name = "Bio User",
            aboutMe = "This is my profile biography."
        )
        db.insertUsers(listOf(user))

        val result = db.getUserById(456)
        assertEquals("This is my profile biography.", result?.aboutMe)
    }

    @Test
    fun insertUsers_replacesExistingUsersOnConflict() = runBlocking {
        val userV1 = createTestUser(id = 1, name = "Original Name", reputation = 10)
        db.insertUsers(listOf(userV1))

        val userV2 = createTestUser(id = 1, name = "Updated Name", reputation = 100)
        db.insertUsers(listOf(userV2))

        val result = db.getUserById(1)
        assertEquals("Updated Name", result?.displayName)
        assertEquals(100, result?.reputation)
    }

    @Test
    fun clearAllUsers_removesAllEntries() = runBlocking {
        val users = listOf(createTestUser(1), createTestUser(2))
        db.insertUsers(users)
        assertEquals(2, db.getAllUsers().size)

        db.clearAllUsers()
        assertTrue(db.getAllUsers().isEmpty())
    }

    @Test
    fun onUpgrade_addsColumnsForVersion4() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("upgrade_test.db")
        
        val dbV1 = object : SQLiteOpenHelper(context, "upgrade_test.db", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, reputation INTEGER, profile_image TEXT, location TEXT, website_url TEXT)")
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }
        
        dbV1.writableDatabase.close()

        val userDb = UserDatabase(context)
        val cursor = userDb.readableDatabase.rawQuery("PRAGMA table_info(users)", null)
        val columns = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                columns.add(it.getString(it.getColumnIndexOrThrow("name")))
            }
        }
        
        assertTrue(columns.contains("bronze_badges"))
        assertTrue(columns.contains("silver_badges"))
        assertTrue(columns.contains("gold_badges"))
        assertTrue(columns.contains("about_me"))
        assertTrue(columns.contains("creation_date"))
        assertTrue(columns.contains("modified_date"))
    }
}
