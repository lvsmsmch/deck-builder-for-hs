package com.lvsmsmch.deckbuilder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.AppNavGraph
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderTheme
import com.lvsmsmch.deckbuilder.util.uiLanguage
import org.koin.compose.koinInject

/** Languages written right to left, by ISO 639 code (both the old and new Hebrew codes). */
private val RTL_LANGUAGES = setOf("ar", "he", "iw", "fa", "ur")

/** Shared application root: theme + navigation, driven by stored preferences. */
@Composable
fun DeckBuilderRoot(onExitApp: () -> Unit = {}) {
    val prefs: PreferencesRepository = koinInject()
    val current by prefs.preferences.collectAsState(initial = null)
    // iOS does not derive the layout direction from the UI language on its own,
    // so Arabic and Hebrew would otherwise read right to left inside a mirrored-
    // the-wrong-way layout.
    val layoutDirection = remember {
        if (uiLanguage() in RTL_LANGUAGES) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        DeckBuilderTheme(themeMode = current?.theme ?: ThemeMode.System) {
            AppNavGraph(
                currentPreferences = current ?: AppPreferences(),
                onExitApp = onExitApp,
            )
        }
    }
}
