package com.lvsmsmch.deckbuilder.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesRepositoryImpl(
    private val store: DataStore<Preferences>,
) : PreferencesRepository {

    override val preferences: Flow<AppPreferences> = store.data.map { it.toDomain() }

    override suspend fun current(): AppPreferences = preferences.first()

    override suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[Keys.theme] = theme.name }
    }

    override suspend fun setCardLocale(locale: String) {
        store.edit { it[Keys.cardLocale] = locale }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        store.edit { it[Keys.crashEnabled] = enabled }
    }

    override suspend fun setLastSeenSetSlug(slug: String?) {
        store.edit {
            if (slug == null) it.remove(Keys.lastSeenSetSlug)
            else it[Keys.lastSeenSetSlug] = slug
        }
    }

    private fun Preferences.toDomain(): AppPreferences = AppPreferences(
        theme = this[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.System,
        cardLocale = this[Keys.cardLocale] ?: "en_US",
        crashReportingEnabled = this[Keys.crashEnabled] ?: true,
        lastSeenSetSlug = this[Keys.lastSeenSetSlug],
    )

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val cardLocale = stringPreferencesKey("card_locale")
        val crashEnabled = booleanPreferencesKey("crash_enabled")
        val lastSeenSetSlug = stringPreferencesKey("last_seen_set_slug")
    }
}
