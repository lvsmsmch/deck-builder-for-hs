package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences

data class SettingsState(
    val prefs: AppPreferences = AppPreferences(),
    val message: String? = null,
)
