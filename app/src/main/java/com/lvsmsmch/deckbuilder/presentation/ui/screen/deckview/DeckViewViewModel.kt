package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import com.lvsmsmch.deckbuilder.domain.usecases.DeleteSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.GetDeckByCodeUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.IsDeckSavedUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.RenameSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SaveDeckUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeckViewViewModel(
    private val code: String,
    private val getDeck: GetDeckByCodeUseCase,
    private val isSaved: IsDeckSavedUseCase,
    private val saveDeck: SaveDeckUseCase,
    private val deleteDeck: DeleteSavedDeckUseCase,
    private val renameDeck: RenameSavedDeckUseCase,
    private val savedRepo: SavedDeckRepository,
    prefs: PreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DeckViewState())
    val state: StateFlow<DeckViewState> = _state.asStateFlow()

    init {
        load()

        // Auto-refetch when the user changes Card language in Settings.
        prefs.preferences
            .map { it.cardLocale }
            .distinctUntilChanged()
            .drop(1)
            .onEach { load() }
            .launchIn(viewModelScope)
    }

    fun load() {
        _state.update { it.copy(deck = UiState.Loading) }
        viewModelScope.launch {
            val saved = isSaved(code)
            val savedName = if (saved) savedRepo.get(code)?.name else null
            when (val r = getDeck(code)) {
                is Result.Success -> _state.update {
                    it.copy(deck = UiState.Loaded(r.data), isSaved = saved, savedName = savedName)
                }
                is Result.Error -> _state.update {
                    it.copy(deck = UiState.Failed(r.throwable), isSaved = saved, savedName = savedName)
                }
            }
        }
    }

    fun toggleSave() {
        val deck = (state.value.deck as? UiState.Loaded)?.data ?: return
        viewModelScope.launch {
            if (state.value.isSaved) {
                deleteDeck(code)
                _state.update { it.copy(isSaved = false, savedName = null) }
            } else {
                saveDeck(deck)
                val name = savedRepo.get(code)?.name
                _state.update { it.copy(isSaved = true, savedName = name) }
            }
        }
    }

    fun rename(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || !state.value.isSaved) return
        viewModelScope.launch {
            renameDeck(code, trimmed)
            _state.update { it.copy(savedName = trimmed) }
        }
    }

    fun toggleStats() {
        _state.update { it.copy(showStats = !it.showStats) }
    }
}
