package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository

class SetThemeUseCase(private val prefs: PreferencesRepository) {
    suspend operator fun invoke(theme: ThemeMode) = prefs.setTheme(theme)
}

class SetCardLocaleUseCase(private val prefs: PreferencesRepository) {
    suspend operator fun invoke(locale: String) = prefs.setCardLocale(locale)
}

class SetCrashReportingEnabledUseCase(private val prefs: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = prefs.setCrashReportingEnabled(enabled)
}

class AcknowledgeNewSetUseCase(private val prefs: PreferencesRepository) {
    suspend operator fun invoke(slug: String) = prefs.setLastSeenSetSlug(slug)
}
