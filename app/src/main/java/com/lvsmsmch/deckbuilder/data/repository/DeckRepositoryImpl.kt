package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.mapper.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeckRepositoryImpl(
    private val api: HearthstoneApi,
    private val metadata: MetadataRepository,
    private val locales: CurrentLocaleProvider,
) : DeckRepository {

    override suspend fun decodeByCode(code: String, locale: String?): Result<Deck> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolved = locales.resolve(locale)
                val meta = currentMetadataOrLoad(resolved)
                api.deckByCode(locale = resolved, code = code).toDomain(meta)
            }
        }

    override suspend fun assembleByIds(
        ids: List<Int>,
        heroCardId: Int?,
        locale: String?,
    ): Result<Deck> = withContext(Dispatchers.IO) {
        runCatchingResult {
            require(ids.isNotEmpty()) { "Deck must have at least one card" }
            val resolved = locales.resolve(locale)
            val meta = currentMetadataOrLoad(resolved)
            api.deckByIds(
                locale = resolved,
                ids = ids.joinToString(","),
                heroCardId = heroCardId,
            ).toDomain(meta)
        }
    }

    private suspend fun currentMetadataOrLoad(locale: String): Metadata =
        metadata.current.value ?: metadata.loadFromCache(locale) ?: Metadata.Empty
}
