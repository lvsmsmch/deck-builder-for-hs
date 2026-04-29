package com.lvsmsmch.deckbuilder.data.hsjson

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persists the cached HearthstoneJSON build per HsJson locale (e.g. `enUS`).
 * Backed by the same DataStore as [com.lvsmsmch.deckbuilder.data.prefs.userPrefsStore].
 */
class HsJsonBuildStore(
    private val store: DataStore<Preferences>,
) {
    suspend fun get(hsJsonLocale: String): String? =
        store.data.map { it[key(hsJsonLocale)] }.first()

    suspend fun set(hsJsonLocale: String, build: String) {
        store.edit { it[key(hsJsonLocale)] = build }
    }

    private fun key(hsJsonLocale: String) =
        stringPreferencesKey("hsjson_build_$hsJsonLocale")
}
