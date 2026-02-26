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

    private lateinit var userStore: SharedPrefsUserStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        context.getSharedPreferences("user_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        userStore = SharedPrefsUserStore(context)
    }

    @Test
    fun saveAndGetIds_returnsCorrectSet() {
        val idsToSave = setOf(1, 2, 3, 42)

        userStore.setFollowedUserIds(idsToSave)
        val retrievedIds = userStore.getFollowedUserIds()
        Assert.assertEquals(idsToSave, retrievedIds)
    }

    @Test
    fun getIds_whenStoreIsEmpty_returnsEmptySet() {
        val retrievedIds = userStore.getFollowedUserIds()
        Assert.assertTrue(retrievedIds.isEmpty())
    }

    @Test
    fun saveEmptySet_overwritesExistingData() {
        userStore.setFollowedUserIds(setOf(1, 2, 3))
        userStore.setFollowedUserIds(emptySet())
        Assert.assertTrue(userStore.getFollowedUserIds().isEmpty())
    }

    @Test
    fun getIds_ignoresNonIntegerStrings() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("user_store", Context.MODE_PRIVATE)
            .edit()
            .putStringSet("followed_ids", setOf("123", "not_a_number", "456"))
            .commit()

        val retrievedIds = userStore.getFollowedUserIds()

        Assert.assertEquals(setOf(123, 456), retrievedIds)
    }
}