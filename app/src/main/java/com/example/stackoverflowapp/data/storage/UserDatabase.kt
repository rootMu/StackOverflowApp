package com.example.stackoverflowapp.data.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.stackoverflowapp.domain.model.User
import androidx.core.database.sqlite.transaction

open class UserDatabase(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REPUTATION = "reputation"
        private const val COLUMN_PROFILE_IMAGE = "profile_image"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_WEBSITE_URL = "website_url"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_REPUTATION INTEGER,
                $COLUMN_PROFILE_IMAGE TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_WEBSITE_URL TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    open fun insertUsers(users: List<User>) {
        val db = writableDatabase
        db.transaction {
            users.forEach { user ->
                val values = ContentValues().apply {
                    put(COLUMN_ID, user.id)
                    put(COLUMN_NAME, user.displayName)
                    put(COLUMN_REPUTATION, user.reputation)
                    put(COLUMN_PROFILE_IMAGE, user.profileImageUrl)
                    put(COLUMN_LOCATION, user.location)
                    put(COLUMN_WEBSITE_URL, user.websiteUrl)
                }
                insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    open fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_REPUTATION DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    users.add(it.toUser())
                } while (it.moveToNext())
            }
        }

        return users
    }

    open fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                it.toUser()
            } else {
                null
            }
        }
    }

    open fun clearAllUsers() {
        writableDatabase.delete(TABLE_USERS, null, null)
    }

    /**
     * Maps the current row of the [Cursor] to a [User] domain model.
     * Assumes the cursor is already positioned at a valid row.
     */
    private fun Cursor.toUser(): User {
        return User(
            id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
            displayName = getString(getColumnIndexOrThrow(COLUMN_NAME)) ?: "",
            reputation = getInt(getColumnIndexOrThrow(COLUMN_REPUTATION)),
            profileImageUrl = getString(getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)),
            location = getString(getColumnIndexOrThrow(COLUMN_LOCATION)),
            websiteUrl = getString(getColumnIndexOrThrow(COLUMN_WEBSITE_URL))
        )
    }
}
