package com.lvsmsmch.deckbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.presentation.ui.navigation.AppNavGraph
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val prefs: PreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val current by prefs.preferences.collectAsState(initial = null)
            val themeMode = current?.theme ?: ThemeMode.System
            DeckBuilderTheme(themeMode = themeMode) {
                AppNavGraph()
            }
        }
    }
}
