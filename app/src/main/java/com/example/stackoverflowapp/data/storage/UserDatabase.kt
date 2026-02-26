package com.example.stackoverflowapp.data.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.stackoverflowapp.domain.model.User
import androidx.core.database.sqlite.transaction

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_REPUTATION = "reputation"
        private const val COLUMN_PROFILE_IMAGE = "profile_image"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_REPUTATION INTEGER,
                $COLUMN_PROFILE_IMAGE TEXT
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

    fun insertUsers(users: List<User>) {
        val db = writableDatabase
        db.transaction {
            try {
                users.forEach { user ->
                    val values = ContentValues().apply {
                        put(COLUMN_ID, user.id)
                        put(COLUMN_NAME, user.displayName)
                        put(COLUMN_REPUTATION, user.reputation)
                        put(COLUMN_PROFILE_IMAGE, user.profileImageUrl)
                    }
                    insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
            } finally {
            }
        }
    }

    fun getAllUsers(): List<User> {
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

        with(cursor) {
           while(moveToNext()) {
               val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
               val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
               val reputation = getInt(getColumnIndexOrThrow(COLUMN_REPUTATION))
               val profileImage = getString(getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE))

               users.add(User(id, name, reputation, profileImage))
           }
        }

        cursor.close()
        return users
    }

    fun clearAllUsers() {
        writableDatabase.delete(TABLE_USERS, null, null)
    }


}