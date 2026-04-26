package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.entities.Page

interface CardBackRepository {

    suspend fun search(
        category: String?,
        textQuery: String,
        page: Int,
        pageSize: Int = 60,
        locale: String? = null,
    ): Result<Page<CardBack>>
}
