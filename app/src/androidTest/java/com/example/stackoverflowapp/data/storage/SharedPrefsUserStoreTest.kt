package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPrefsUserStoreTest {

    private lateinit var store: SharedPrefsUserStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("user_store", Context.MODE_PRIVATE).edit().clear().commit()
        store = SharedPrefsUserStore(context)
    }

    @Test
    fun getFollowedUserIds_initiallyEmpty() {
        assertTrue(store.getFollowedUserIds().isEmpty())
    }

    @Test
    fun setFollowedUserIds_persistsMultipleIds() {
        val ids = setOf(1, 2, 100)
        store.setFollowedUserIds(ids)
        assertEquals(ids, store.getFollowedUserIds())
    }

    @Test
    fun setFollowedUserIds_overwritesPreviousData() {
        store.setFollowedUserIds(setOf(1, 2))
        val newIds = setOf(3, 4)
        store.setFollowedUserIds(newIds)
        assertEquals(newIds, store.getFollowedUserIds())
    }

    @Test
    fun setFollowedUserIds_withEmptySet_clearsData() {
        store.setFollowedUserIds(setOf(1, 2))
        store.setFollowedUserIds(emptySet())
        assertTrue(store.getFollowedUserIds().isEmpty())
    }

    @Test
    fun getFollowedUserIds_handlesMalformedData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("user_store", Context.MODE_PRIVATE)
        
        prefs.edit().putStringSet("followed_ids", setOf("1", "abc", "2")).commit()
        
        val result = store.getFollowedUserIds()
        assertEquals(setOf(1, 2), result)
    }
}
