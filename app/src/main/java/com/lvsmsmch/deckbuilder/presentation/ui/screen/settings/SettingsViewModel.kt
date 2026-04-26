package com.lvsmsmch.deckbuilder.presentation.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.usecases.ObservePreferencesUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.RefreshMetadataUseCase
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
    metadata: MetadataRepository,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCardLocale: SetCardLocaleUseCase,
    private val setCrashReporting: SetCrashReportingEnabledUseCase,
    private val refreshMetadata: RefreshMetadataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePrefs()
            .onEach { prefs -> _state.update { it.copy(prefs = prefs) } }
            .launchIn(viewModelScope)
        metadata.current
            .onEach { meta -> _state.update { it.copy(metadataRefreshedAtMs = meta?.refreshedAtMs) } }
            .launchIn(viewModelScope)
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { setThemeUseCase(theme) }
    }

    fun setLocale(code: String) {
        viewModelScope.launch {
            setCardLocale(code)
            // Locale changed — refetch metadata in the new language so cards
            // resolved going forward use the localised names.
            forceMetadataRefresh()
        }
    }

    fun setCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch { setCrashReporting(enabled) }
    }

    fun refreshMetadataNow() {
        viewModelScope.launch { forceMetadataRefresh() }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    private suspend fun forceMetadataRefresh() {
        _state.update { it.copy(isRefreshingMetadata = true) }
        val r = refreshMetadata(force = true)
        val msg = when (r) {
            is Result.Success -> "Metadata refreshed"
            is Result.Error -> "Refresh failed: ${r.throwable.message ?: r.throwable.javaClass.simpleName}"
        }
        _state.update { it.copy(isRefreshingMetadata = false, message = msg) }
    }
}
