package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository

class AssembleDeckUseCase(
    private val decks: DeckRepository,
) {
    suspend operator fun invoke(
        ids: List<Int>,
        heroCardId: Int?,
        locale: String? = null,
    ): Result<Deck> = decks.assembleByIds(ids, heroCardId, locale)
}
