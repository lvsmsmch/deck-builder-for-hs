package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository

class GetCardDetailsUseCase(
    private val cards: CardRepository,
) {
    fun cached(idOrSlug: String): Card? = cards.cachedCard(idOrSlug)

    suspend operator fun invoke(idOrSlug: String, locale: String? = null): Result<Card> =
        cards.getCard(idOrSlug, locale)
}
