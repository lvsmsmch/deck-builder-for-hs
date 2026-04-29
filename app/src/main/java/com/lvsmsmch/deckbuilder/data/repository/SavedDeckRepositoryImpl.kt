package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

private const val TAG = "DB.SavedDeckRepo"

class SavedDeckRepositoryImpl(
    private val dao: SavedDeckDao,
    private val nowMs: () -> Long = System::currentTimeMillis,
) : SavedDeckRepository {

    override fun observeAll(): Flow<List<DeckPreview>> =
        dao.observeAll()
            .onEach { rows -> Log.d(TAG, "observeAll: emit ${rows.size} rows") }
            .map { rows -> rows.map(::toPreview) }
            .flowOn(Dispatchers.Default)

    override suspend fun isSaved(code: String): Boolean = withContext(Dispatchers.IO) {
        val exists = dao.exists(code)
        Log.d(TAG, "isSaved: code='${code.take(12)}…' → $exists")
        exists
    }

    override suspend fun save(deck: Deck, name: String?): Unit = withContext(Dispatchers.IO) {
        require(deck.code.isNotBlank()) { "Deck.code is empty — cannot save" }
        try {
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
                    heroSlug = deck.hero?.slug,
                    format = deck.format.apiSlug,
                    cardCount = deck.cardCount,
                    cardIdsCsv = cardIdsCsv,
                    createdAtMs = existing?.createdAtMs ?: now,
                    updatedAtMs = now,
                )
            )
            Log.i(TAG, "save: OK code='${deck.code.take(12)}…' name='$resolvedName' cards=${deck.cardCount} ${if (existing == null) "[new]" else "[update]"}")
        } catch (t: Throwable) {
            Log.w(TAG, "save: FAILED code='${deck.code.take(12)}…': ${t.message}", t)
            throw t
        }
    }

    override suspend fun delete(code: String): Unit = withContext(Dispatchers.IO) {
        try {
            dao.delete(code)
            Log.i(TAG, "delete: OK code='${code.take(12)}…'")
        } catch (t: Throwable) {
            Log.w(TAG, "delete: FAILED code='${code.take(12)}…': ${t.message}", t)
            throw t
        }
    }

    private fun toPreview(row: SavedDeckEntity): DeckPreview = DeckPreview(
        code = row.code,
        name = row.name,
        classSlug = row.classSlug,
        className = row.className,
        heroCardId = row.heroCardId,
        heroSlug = row.heroSlug,
        format = GameFormat.fromApi(row.format),
        cardCount = row.cardCount,
        savedAtMs = row.updatedAtMs,
    )

    private fun defaultName(deck: Deck): String {
        val cls = deck.heroClass?.name?.ifBlank { null }
        return cls?.let { "$it deck" } ?: "Untitled deck"
    }
}
