package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Deck

interface DeckRepository {

    suspend fun decodeByCode(code: String, locale: String? = null): Result<Deck>

    suspend fun assembleByIds(
        ids: List<Int>,
        heroCardId: Int? = null,
        locale: String? = null,
    ): Result<Deck>
}
