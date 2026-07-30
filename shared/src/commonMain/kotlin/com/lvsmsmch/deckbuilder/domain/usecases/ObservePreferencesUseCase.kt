package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObservePreferencesUseCase(
    private val prefs: PreferencesRepository,
) {
    operator fun invoke(): Flow<AppPreferences> = prefs.preferences
}
