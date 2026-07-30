package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository

class SearchCardsUseCase(
    private val cards: CardRepository,
) {
    suspend operator fun invoke(
        filters: CardFilters,
        page: Int,
        pageSize: Int = 60,
        locale: String? = null,
    ): Result<Page<Card>> = cards.searchCards(filters = filters, page = page, pageSize = pageSize, locale = locale)
}
