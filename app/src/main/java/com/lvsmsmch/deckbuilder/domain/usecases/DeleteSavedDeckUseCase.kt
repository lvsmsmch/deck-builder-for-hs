package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository

class DeleteSavedDeckUseCase(
    private val saved: SavedDeckRepository,
) {
    suspend operator fun invoke(code: String) = saved.delete(code)
}
