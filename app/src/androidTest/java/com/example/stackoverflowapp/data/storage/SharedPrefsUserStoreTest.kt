package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPrefsUserStoreTest {

    private lateinit var store: SharedPrefsUserStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = SharedPrefsUserStore(context)
    }

    private fun assertStorePersistence(ids: Set<Int>) {
        store.setFollowedUserIds(ids)
        Assert.assertEquals(ids, store.getFollowedUserIds())
    }

    @Test
    fun saveAndRetrieveMultipleIDs() {
        assertStorePersistence(setOf(1, 2, 100))
    }

    @Test
    fun savingEmptySetClearsPreviousData() {
        store.setFollowedUserIds(setOf(1))
        assertStorePersistence(emptySet())
    }
}