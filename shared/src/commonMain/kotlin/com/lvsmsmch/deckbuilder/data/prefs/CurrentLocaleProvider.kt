package com.lvsmsmch.deckbuilder.data.prefs

import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository

/**
 * Cheap synchronous-feeling locale read for repository hot paths.
 * Repos call [resolve] when no explicit locale is supplied.
 */
class CurrentLocaleProvider(
    private val prefs: PreferencesRepository,
) {
    suspend fun resolve(explicit: String? = null): String =
        explicit ?: prefs.current().cardLocale
}
