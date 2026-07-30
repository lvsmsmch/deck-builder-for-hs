package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card

data class CardDetailState(
    val card: UiState<Card> = UiState.Idle,
    val isStandardLegal: Boolean? = null,
    val relatedCards: List<Card> = emptyList(),
    val isLoadingRelated: Boolean = false,
)
