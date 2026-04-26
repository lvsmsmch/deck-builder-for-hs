package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.data.db.dao.SavedDeckDao
import com.lvsmsmch.deckbuilder.data.db.entity.SavedDeckEntity
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SavedDeckRepositoryImpl(
    private val dao: SavedDeckDao,
    private val nowMs: () -> Long = System::currentTimeMillis,
) : SavedDeckRepository {

    override fun observeAll(): Flow<List<DeckPreview>> =
        dao.observeAll()
            .map { rows -> rows.map(::toPreview) }
            .flowOn(Dispatchers.Default)

    override suspend fun isSaved(code: String): Boolean = withContext(Dispatchers.IO) {
        dao.exists(code)
    }

    override suspend fun save(deck: Deck, name: String?) = withContext(Dispatchers.IO) {
        require(deck.code.isNotBlank()) { "Deck.code is empty — cannot save" }
        val now = nowMs()
        val existing = dao.get(deck.code)
        val cardIdsCsv = deck.cards
            .flatMap { entry -> List(entry.count) { entry.card.id } }
            .joinToString(",")
        val resolvedName = name?.takeIf { it.isNotBlank() }
            ?: existing?.name
            ?: defaultName(deck)
        dao.upsert(
            SavedDeckEntity(
                code = deck.code,
                name = resolvedName,
                classSlug = deck.heroClass?.slug,
                className = deck.heroClass?.name,
                heroCardId = deck.hero?.id ?: 0,
                format = deck.format.apiSlug,
                cardCount = deck.cardCount,
                cardIdsCsv = cardIdsCsv,
                createdAtMs = existing?.createdAtMs ?: now,
                updatedAtMs = now,
            )
        )
    }

    override suspend fun delete(code: String) = withContext(Dispatchers.IO) {
        dao.delete(code)
    }

    private fun toPreview(row: SavedDeckEntity): DeckPreview = DeckPreview(
        code = row.code,
        name = row.name,
        classSlug = row.classSlug,
        className = row.className,
        heroCardId = row.heroCardId,
        format = GameFormat.fromApi(row.format),
        cardCount = row.cardCount,
        savedAtMs = row.updatedAtMs,
    )

    private fun defaultName(deck: Deck): String {
        val cls = deck.heroClass?.name?.ifBlank { null }
        return cls?.let { "$it deck" } ?: "Untitled deck"
    }
}
