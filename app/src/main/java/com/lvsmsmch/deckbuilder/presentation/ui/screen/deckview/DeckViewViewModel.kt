package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.usecases.DeleteSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.GetDeckByCodeUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.IsDeckSavedUseCase
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
    metadata: MetadataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DeckViewState())
    val state: StateFlow<DeckViewState> = _state.asStateFlow()

    init {
        load()

        // Auto-refetch when the user changes Card language in Settings.
        metadata.current
            .map { it?.locale }
            .distinctUntilChanged()
            .drop(1)
            .onEach { load() }
            .launchIn(viewModelScope)
    }

    fun load() {
        _state.update { it.copy(deck = UiState.Loading) }
        viewModelScope.launch {
            val saved = isSaved(code)
            when (val r = getDeck(code)) {
                is Result.Success -> _state.update {
                    it.copy(deck = UiState.Loaded(r.data), isSaved = saved)
                }
                is Result.Error -> _state.update {
                    it.copy(deck = UiState.Failed(r.throwable), isSaved = saved)
                }
            }
        }
    }

    fun toggleSave() {
        val deck = (state.value.deck as? UiState.Loaded)?.data ?: return
        viewModelScope.launch {
            if (state.value.isSaved) {
                deleteDeck(code)
                _state.update { it.copy(isSaved = false) }
            } else {
                saveDeck(deck)
                _state.update { it.copy(isSaved = true) }
            }
        }
    }

    fun toggleStats() {
        _state.update { it.copy(showStats = !it.showStats) }
    }
}
