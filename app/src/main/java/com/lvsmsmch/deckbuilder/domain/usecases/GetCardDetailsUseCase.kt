package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository

class GetCardDetailsUseCase(
    private val cards: CardRepository,
) {
    suspend operator fun invoke(idOrSlug: String, locale: String? = null): Result<Card> =
        cards.getCard(idOrSlug, locale)
}
