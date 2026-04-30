package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository

class RenameSavedDeckUseCase(
    private val saved: SavedDeckRepository,
) {
    suspend operator fun invoke(code: String, name: String) {
        saved.rename(code, name)
    }
}
