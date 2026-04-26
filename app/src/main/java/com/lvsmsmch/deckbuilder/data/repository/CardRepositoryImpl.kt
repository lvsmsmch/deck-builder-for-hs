package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.mapper.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CardRepositoryImpl(
    private val api: HearthstoneApi,
    private val metadata: MetadataRepository,
    private val locales: CurrentLocaleProvider,
) : CardRepository {

    override suspend fun getCard(idOrSlug: String, locale: String?): Result<Card> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolved = locales.resolve(locale)
                val meta = currentMetadataOrLoad(resolved)
                api.card(idOrSlug = idOrSlug, locale = resolved).toDomain(meta)
            }
        }

    override suspend fun searchCards(
        filters: CardFilters,
        page: Int,
        pageSize: Int,
        locale: String?,
    ): Result<Page<Card>> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val resolved = locales.resolve(locale)
            val meta = currentMetadataOrLoad(resolved)
            val params = buildSearchParams(filters, page, pageSize, resolved)
            val resp = api.searchCards(params)
            Page(
                items = resp.cards.map { it.toDomain(meta) },
                pageNumber = resp.page,
                pageCount = resp.pageCount,
                totalCount = resp.cardCount,
            )
        }
    }

    private suspend fun currentMetadataOrLoad(locale: String): Metadata =
        metadata.current.value ?: metadata.loadFromCache(locale) ?: Metadata.Empty

    private fun buildSearchParams(
        filters: CardFilters,
        page: Int,
        pageSize: Int,
        locale: String,
    ): Map<String, String> = buildMap {
        put("locale", locale)
        put("page", page.toString())
        put("pageSize", pageSize.toString())
        put("gameMode", filters.gameMode.apiSlug)
        put("sort", filters.sort.toApiParam())
        put("collectible", if (filters.collectibleOnly) "1" else "0,1")

        if (filters.textQuery.isNotBlank()) put("textFilter", filters.textQuery.trim())
        if (filters.classes.isNotEmpty()) put("class", filters.classes.joinToString(","))
        if (filters.sets.isNotEmpty()) put("set", filters.sets.joinToString(","))
        if (filters.rarities.isNotEmpty()) put("rarity", filters.rarities.joinToString(","))
        if (filters.types.isNotEmpty()) put("type", filters.types.joinToString(","))
        if (filters.minionTypes.isNotEmpty()) put("minionType", filters.minionTypes.joinToString(","))
        if (filters.keywords.isNotEmpty()) put("keyword", filters.keywords.joinToString(","))
        if (filters.spellSchools.isNotEmpty()) put("spellSchool", filters.spellSchools.joinToString(","))
        if (filters.manaCosts.isNotEmpty()) {
            // UI chip "7+" comes through as `7`; the API caps at 10, with 10 meaning
            // "10 or more". Expand 7 to 7,8,9,10 so the user gets every high-cost card.
            val expanded = filters.manaCosts.flatMap { cost ->
                if (cost >= 7) (7..10).toList() else listOf(cost)
            }.toSortedSet()
            put("manaCost", expanded.joinToString(","))
        }
        if (filters.gameMode == CardFilters.GameMode.BATTLEGROUNDS && filters.tiers.isNotEmpty()) {
            put("tier", filters.tiers.joinToString(","))
        }
    }
}
