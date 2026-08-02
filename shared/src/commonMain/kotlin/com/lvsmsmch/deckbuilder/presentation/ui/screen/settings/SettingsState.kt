package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import com.lvsmsmch.deckbuilder.presentation.UiText

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences

data class SettingsState(
    val prefs: AppPreferences = AppPreferences(),
    val message: UiText? = null,
    val cardsBuild: String? = null,
    val cardCount: Int = 0,
    val cardDataBytes: Long = 0L,
    val isRefreshingCardData: Boolean = false,
)
