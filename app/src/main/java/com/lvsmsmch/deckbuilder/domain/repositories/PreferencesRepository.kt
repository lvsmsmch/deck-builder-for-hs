package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val preferences: Flow<AppPreferences>

    /** Snapshot read — used by repos that need the locale on a hot path. */
    suspend fun current(): AppPreferences

    suspend fun setTheme(theme: ThemeMode)
    suspend fun setCardLocale(locale: String)
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    suspend fun setLastSeenSetSlug(slug: String?)
}
