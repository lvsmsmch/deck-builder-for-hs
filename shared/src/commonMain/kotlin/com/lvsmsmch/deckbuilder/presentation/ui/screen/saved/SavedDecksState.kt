package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.presentation.UiText

/** How the saved-deck list is ordered. */
enum class DeckSort { Updated, Name, Size }

data class SavedDecksState(
    val decks: List<DeckPreview> = emptyList(),
    val sort: DeckSort = DeckSort.Updated,
    val importInProgress: Boolean = false,
    val importError: UiText? = null,
) {
    /** Decks in the chosen order; the repository always emits newest-first. */
    val sortedDecks: List<DeckPreview>
        get() = when (sort) {
            DeckSort.Updated -> decks.sortedByDescending { it.savedAtMs }
            DeckSort.Name -> decks.sortedBy { it.name.lowercase() }
            DeckSort.Size -> decks.sortedByDescending { it.cardCount }
        }

    val totalCards: Int get() = decks.sumOf { it.cardCount }
}
