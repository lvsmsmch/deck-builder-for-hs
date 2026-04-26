package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Deck

data class DeckViewState(
    val deck: UiState<Deck> = UiState.Idle,
    val isSaved: Boolean = false,
    val showStats: Boolean = true,
)
