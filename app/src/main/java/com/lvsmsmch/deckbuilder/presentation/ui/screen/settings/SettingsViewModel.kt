package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.update.UpdateRunner
import com.lvsmsmch.deckbuilder.domain.entities.AppPreferences
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.usecases.ObservePreferencesUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCardLocaleUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetCrashReportingEnabledUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SetThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    observePrefs: ObservePreferencesUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCardLocale: SetCardLocaleUseCase,
    private val setCrashReporting: SetCrashReportingEnabledUseCase,
    private val hsJson: HsJsonRepository,
    private val updateRunner: UpdateRunner,
    initialPreferences: AppPreferences = AppPreferences(),
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(prefs = initialPreferences))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePrefs()
            .onEach { prefs ->
                _state.update { it.copy(prefs = prefs) }
            }
            .distinctUntilChangedBy { it.cardLocale }
            .onEach { prefs ->
                refreshBuildLabel(prefs.cardLocale)
            }
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

    fun refreshCardDataNow() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshingCardData = true, message = null) }
            val result = runCatching { updateRunner.runOnce(reason = "card data screen") }
            refreshBuildLabel(_state.value.prefs.cardLocale)
            _state.update {
                it.copy(
                    isRefreshingCardData = false,
                    message = if (result.isSuccess) "Card data checked" else "Refresh failed",
                )
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    private suspend fun refreshBuildLabel(locale: String) {
        val build = runCatching { hsJson.currentBuild(locale) }.getOrNull()
        _state.update { it.copy(cardsBuild = build) }
    }
}
