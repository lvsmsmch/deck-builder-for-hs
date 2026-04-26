package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.usecases.DeleteSavedDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.ImportDeckByCodeUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.InvalidDeckCodeException
import com.lvsmsmch.deckbuilder.domain.usecases.ObserveSavedDecksUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SaveDeckUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedDecksViewModel(
    observeSaved: ObserveSavedDecksUseCase,
    private val importByCode: ImportDeckByCodeUseCase,
    private val saveDeck: SaveDeckUseCase,
    private val deleteSaved: DeleteSavedDeckUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedDecksState())
    val state: StateFlow<SavedDecksState> = _state.asStateFlow()

    private val _navEffects = Channel<NavEffect>(Channel.BUFFERED)
    val navEffects = _navEffects.receiveAsFlow()

    private var importJob: Job? = null

    init {
        observeSaved()
            .onEach { decks -> _state.update { it.copy(decks = decks) } }
            .launchIn(viewModelScope)
    }

    fun import(code: String) {
        importJob?.cancel()
        _state.update { it.copy(importInProgress = true, importError = null) }
        importJob = viewModelScope.launch {
            when (val r = importByCode(code)) {
                is Result.Success -> {
                    saveDeck(r.data)
                    _state.update { it.copy(importInProgress = false, importError = null) }
                    _navEffects.trySend(NavEffect.OpenDeck(r.data.code))
                }
                is Result.Error -> _state.update {
                    it.copy(
                        importInProgress = false,
                        importError = friendlyError(r.throwable),
                    )
                }
            }
        }
    }

    fun delete(code: String) {
        viewModelScope.launch { deleteSaved(code) }
    }

    fun clearImportError() {
        _state.update { it.copy(importError = null) }
    }

    private fun friendlyError(t: Throwable): String = when (t) {
        is InvalidDeckCodeException -> "This deck code looks invalid."
        else -> t.message ?: t.javaClass.simpleName
    }

    sealed interface NavEffect {
        data class OpenDeck(val code: String) : NavEffect
    }
}
