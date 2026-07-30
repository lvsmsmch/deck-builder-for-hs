package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository

class SaveDeckUseCase(
    private val saved: SavedDeckRepository,
) {
    suspend operator fun invoke(deck: Deck, name: String? = null) {
        saved.save(deck, name)
    }
}
