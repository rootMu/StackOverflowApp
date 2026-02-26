package com.example.stackoverflowapp.data.storage

import android.content.Context
import androidx.core.content.edit

class SharedPrefsUserStore(context: Context): UserStore {

    private val sharedPreferences = context.getSharedPreferences("user_store", Context.MODE_PRIVATE)
    private val KEY_FOLLOWED_IDS = "followed_ids"

    override fun getFollowedUserIds(): Set<Int> =
        sharedPreferences
            .getStringSet(KEY_FOLLOWED_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()


    override fun setFollowedUserIds(ids: Set<Int>) =
        sharedPreferences.edit {
            putStringSet(KEY_FOLLOWED_IDS, ids.map { it.toString() }.toSet())
        }



}