package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Page

interface CardRepository {

    /** When [locale] is null, the impl falls back to the user's current pref. */
    suspend fun getCard(idOrSlug: String, locale: String? = null): Result<Card>

    suspend fun searchCards(
        filters: CardFilters,
        page: Int,
        pageSize: Int = 60,
        locale: String? = null,
    ): Result<Page<Card>>
}
