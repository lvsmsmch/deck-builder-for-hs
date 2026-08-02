package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import com.lvsmsmch.deckbuilder.presentation.UiText

import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview

data class SavedDecksState(
    val decks: List<DeckPreview> = emptyList(),
    val importInProgress: Boolean = false,
    val importError: UiText? = null,
)
