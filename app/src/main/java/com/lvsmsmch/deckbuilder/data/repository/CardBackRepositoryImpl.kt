package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardBackRepository

// Card Backs feature is dropped in phase 8; this stub keeps DI alive until the
// screen and use case are removed. No network, no data.
class CardBackRepositoryImpl : CardBackRepository {

    override suspend fun search(
        category: String?,
        textQuery: String,
        page: Int,
        pageSize: Int,
        locale: String?,
    ): Result<Page<CardBack>> = Result.Success(
        Page(items = emptyList(), pageNumber = 1, pageCount = 1, totalCount = 0),
    )
}
