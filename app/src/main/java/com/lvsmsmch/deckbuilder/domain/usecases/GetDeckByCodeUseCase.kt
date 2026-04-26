package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository

class GetDeckByCodeUseCase(
    private val decks: DeckRepository,
) {
    suspend operator fun invoke(code: String, locale: String? = null): Result<Deck> =
        decks.decodeByCode(code, locale)
}
