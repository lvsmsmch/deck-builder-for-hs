package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardBackRepository

class SearchCardBacksUseCase(
    private val repo: CardBackRepository,
) {
    suspend operator fun invoke(
        category: String? = null,
        textQuery: String = "",
        page: Int = 1,
        pageSize: Int = 60,
        locale: String? = null,
    ): Result<Page<CardBack>> = repo.search(category, textQuery, page, pageSize, locale)
}
