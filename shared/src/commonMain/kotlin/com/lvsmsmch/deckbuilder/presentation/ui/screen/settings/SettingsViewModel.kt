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

    private val _state = MutableStateFlow(
        SettingsState(
            prefs = initialPreferences,
            cardsBuild = hsJson.cachedBuild(initialPreferences.cardLocale),
        ),
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePrefs()
            .onEach { prefs ->
                _state.update { it.copy(prefs = prefs) }
            }
            .distinctUntilChangedBy { it.cardLocale }
            .onEach { prefs ->
                refreshCardDataMetadata(prefs.cardLocale)
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
            refreshCardDataMetadata(_state.value.prefs.cardLocale)
            _state.update {
                it.copy(
                    isRefreshingCardData = false,
                    message = if (result.isSuccess) "Card data refreshed" else "Refresh failed",
                )
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    fun showMessage(text: String) {
        _state.update { it.copy(message = text) }
    }

    fun refreshCardDataMetadata() {
        viewModelScope.launch { refreshCardDataMetadata(_state.value.prefs.cardLocale) }
    }

    private suspend fun refreshCardDataMetadata(locale: String) {
        val build = runCatching { hsJson.currentBuild(locale) }.getOrNull()
        val stats = runCatching { hsJson.cachedStats(locale) }.getOrNull()
        _state.update {
            it.copy(
                cardsBuild = build,
                cardCount = stats?.count ?: 0,
                cardDataBytes = stats?.bytes ?: 0L,
            )
        }
    }
}
