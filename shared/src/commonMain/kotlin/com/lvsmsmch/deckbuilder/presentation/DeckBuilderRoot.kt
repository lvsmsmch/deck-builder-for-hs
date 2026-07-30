package com.lvsmsmch.deckbuilder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.AppNavGraph
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderTheme
import org.koin.compose.koinInject

/** Shared application root: theme + navigation, driven by stored preferences. */
@Composable
fun DeckBuilderRoot(onExitApp: () -> Unit = {}) {
    val prefs: PreferencesRepository = koinInject()
    val current by prefs.preferences.collectAsState(initial = null)
    DeckBuilderTheme(themeMode = current?.theme ?: ThemeMode.System) {
        AppNavGraph(
            currentPreferences = current ?: AppPreferences(),
            onExitApp = onExitApp,
        )
    }
}
