package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.usecases.GetCardDetailsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

class CardDetailViewModel(
    private val idOrSlug: String,
    private val getCardDetails: GetCardDetailsUseCase,
    metadata: MetadataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CardDetailState())
    val state: StateFlow<CardDetailState> = _state.asStateFlow()

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
        _state.update { it.copy(card = UiState.Loading) }
        viewModelScope.launch {
            when (val r = getCardDetails(idOrSlug)) {
                is Result.Success -> {
                    _state.update { it.copy(card = UiState.Loaded(r.data)) }
                    fetchRelated(r.data.childIds)
                }
                is Result.Error -> _state.update { it.copy(card = UiState.Failed(r.throwable)) }
            }
        }
    }

    /**
     * `childIds` can be huge for cards that summon many tokens or for spells like
     * Yogg-Saron. Cap to keep the detail screen reasonable.
     */
    private fun fetchRelated(childIds: List<Int>) {
        if (childIds.isEmpty()) {
            _state.update { it.copy(relatedCards = emptyList()) }
            return
        }
        val limited = childIds.take(MAX_RELATED)
        _state.update { it.copy(isLoadingRelated = true) }
        viewModelScope.launch {
            val cards: List<Card> = limited.map { id ->
                async { (getCardDetails(id.toString()) as? Result.Success)?.data }
            }.awaitAll().filterNotNull()
            _state.update { it.copy(relatedCards = cards, isLoadingRelated = false) }
        }
    }

    private companion object {
        const val MAX_RELATED = 8
    }
}
