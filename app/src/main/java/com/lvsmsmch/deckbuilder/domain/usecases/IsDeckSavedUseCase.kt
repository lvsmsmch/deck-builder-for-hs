package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository

class IsDeckSavedUseCase(
    private val saved: SavedDeckRepository,
) {
    suspend operator fun invoke(code: String): Boolean = saved.isSaved(code)
}
