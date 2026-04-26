package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavedDecksUseCase(
    private val saved: SavedDeckRepository,
) {
    operator fun invoke(): Flow<List<DeckPreview>> = saved.observeAll()
}
