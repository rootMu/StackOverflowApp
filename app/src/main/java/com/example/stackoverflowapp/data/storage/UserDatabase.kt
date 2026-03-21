package com.example.stackoverflowapp.data.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.example.stackoverflowapp.domain.model.BadgeCounts
import com.example.stackoverflowapp.domain.model.User

open class UserDatabase(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), UserLocalDataSource {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 4

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REPUTATION = "reputation"
        private const val COLUMN_PROFILE_IMAGE = "profile_image"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_WEBSITE_URL = "website_url"
        private const val COLUMN_BRONZE_BADGES = "bronze_badges"
        private const val COLUMN_SILVER_BADGES = "silver_badges"
        private const val COLUMN_GOLD_BADGES = "gold_badges"
        private const val COLUMN_ABOUT_ME = "about_me"
        private const val COLUMN_CREATION_DATE = "creation_date"
        private const val COLUMN_MODIFIED_DATE = "modified_date"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_REPUTATION INTEGER,
                $COLUMN_PROFILE_IMAGE TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_WEBSITE_URL TEXT,
                $COLUMN_BRONZE_BADGES INTEGER DEFAULT 0,
                $COLUMN_SILVER_BADGES INTEGER DEFAULT 0,
                $COLUMN_GOLD_BADGES INTEGER DEFAULT 0,
                $COLUMN_ABOUT_ME TEXT,
                $COLUMN_CREATION_DATE INTEGER,
                $COLUMN_MODIFIED_DATE INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_BRONZE_BADGES INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_SILVER_BADGES INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_GOLD_BADGES INTEGER DEFAULT 0")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_ABOUT_ME TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_CREATION_DATE INTEGER")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_MODIFIED_DATE INTEGER")
        }
    }

    override suspend fun insertUsers(users: List<User>) {
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
                    put(COLUMN_BRONZE_BADGES, user.badgeCounts?.bronze ?: 0)
                    put(COLUMN_SILVER_BADGES, user.badgeCounts?.silver ?: 0)
                    put(COLUMN_GOLD_BADGES, user.badgeCounts?.gold ?: 0)
                    put(COLUMN_ABOUT_ME, user.aboutMe)
                    put(COLUMN_CREATION_DATE, user.creationDate)
                    put(COLUMN_MODIFIED_DATE, user.lastModifiedDate)
                }
                insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    override suspend fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            null
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

    override suspend fun getUserById(userId: Int): User? {
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

    override suspend fun clearAllUsers() {
        writableDatabase.delete(TABLE_USERS, null, null)
    }

    private fun Cursor.toUser(): User {
        return User(
            id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
            displayName = getString(getColumnIndexOrThrow(COLUMN_NAME)) ?: "",
            reputation = getInt(getColumnIndexOrThrow(COLUMN_REPUTATION)),
            profileImageUrl = getString(getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)),
            location = getString(getColumnIndexOrThrow(COLUMN_LOCATION)),
            websiteUrl = getString(getColumnIndexOrThrow(COLUMN_WEBSITE_URL)),
            badgeCounts = BadgeCounts(
                bronze = getInt(getColumnIndexOrThrow(COLUMN_BRONZE_BADGES)),
                silver = getInt(getColumnIndexOrThrow(COLUMN_SILVER_BADGES)),
                gold = getInt(getColumnIndexOrThrow(COLUMN_GOLD_BADGES))
            ),
            aboutMe = getString(getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
            creationDate = if (isNull(getColumnIndexOrThrow(COLUMN_CREATION_DATE))) null else getLong(getColumnIndexOrThrow(COLUMN_CREATION_DATE)),
            lastModifiedDate = if (isNull(getColumnIndexOrThrow(COLUMN_MODIFIED_DATE))) null else getLong(getColumnIndexOrThrow(COLUMN_MODIFIED_DATE))
        )
    }
}
