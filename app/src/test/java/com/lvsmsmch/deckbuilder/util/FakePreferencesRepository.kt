package com.lvsmsmch.deckbuilder.util

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Synchronous in-memory PreferencesRepository for ViewModel tests. Real DataStore-backed
 * tests live in PreferencesRepositoryImplTest; here we sidestep DataStore so VM tests
 * stay free of IO dispatcher / Looper initialization races.
 */
class FakePreferencesRepository(
    initial: AppPreferences = AppPreferences(),
) : PreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<AppPreferences> = state

    override suspend fun current(): AppPreferences = state.value
    override suspend fun setTheme(theme: ThemeMode) {
        state.update { it.copy(theme = theme) }
    }
    override suspend fun setCardLocale(locale: String) {
        state.update { it.copy(cardLocale = locale) }
    }
    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        state.update { it.copy(crashReportingEnabled = enabled) }
    }
    override suspend fun setLastSeenSetSlug(slug: String?) {
        state.update { it.copy(lastSeenSetSlug = slug) }
    }
}
