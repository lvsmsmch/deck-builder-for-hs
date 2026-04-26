package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences

data class SettingsState(
    val prefs: AppPreferences = AppPreferences(),
    val isRefreshingMetadata: Boolean = false,
    val metadataRefreshedAtMs: Long? = null,
    val message: String? = null,
)
