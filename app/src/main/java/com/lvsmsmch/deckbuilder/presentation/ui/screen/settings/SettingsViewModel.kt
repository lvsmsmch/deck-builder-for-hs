package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.usecases.ObservePreferencesUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCardLocaleUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCrashReportingEnabledUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    observePrefs: ObservePreferencesUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCardLocale: SetCardLocaleUseCase,
    private val setCrashReporting: SetCrashReportingEnabledUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePrefs()
            .onEach { prefs -> _state.update { it.copy(prefs = prefs) } }
            .launchIn(viewModelScope)
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { setThemeUseCase(theme) }
    }

    fun setLocale(code: String) {
        viewModelScope.launch { setCardLocale(code) }
    }

    fun setCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch { setCrashReporting(enabled) }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }
}
