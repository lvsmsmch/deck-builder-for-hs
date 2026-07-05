package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.db.dao.SavedDeckDao
import com.lvsmsmch.deckbuilder.data.db.entity.SavedDeckEntity
import com.lvsmsmch.deckbuilder.data.deckstring.Deckstring
import com.lvsmsmch.deckbuilder.data.deckstring.DeckstringFormat
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
            val safeName = resolvedName.take(MAX_DECK_NAME_LENGTH)
            dao.upsert(
                SavedDeckEntity(
                    code = deck.code,
                    name = safeName,
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

    override suspend fun rename(code: String, name: String): Unit = withContext(Dispatchers.IO) {
        require(code.isNotBlank()) { "Deck code is empty — cannot rename" }
        val trimmed = name.trim().take(MAX_DECK_NAME_LENGTH)
        require(trimmed.isNotEmpty()) { "Deck name cannot be empty" }
        dao.rename(code, trimmed, nowMs())
        Log.i(TAG, "rename: OK code='${code.take(12)}…' → '$trimmed'")
    }

    override suspend fun get(code: String): DeckPreview? = withContext(Dispatchers.IO) {
        dao.get(code)?.let(::toPreview)
    }

    private fun toPreview(row: SavedDeckEntity): DeckPreview = DeckPreview(
        code = row.code,
        name = row.name,
        classSlug = row.classSlug,
        className = row.className,
        heroCardId = row.heroCardId,
        heroSlug = row.heroSlug,
        format = row.formatFromCode(),
        cardCount = row.cardCount,
        maxCardCount = maxCardCountFor(row.cardIdsCsv),
        savedAtMs = row.updatedAtMs,
    )

    private fun defaultName(deck: Deck): String {
        val cls = deck.heroClass?.name?.ifBlank { null }
        return cls?.let { "$it deck" } ?: "Untitled deck"
    }
}

private fun maxCardCountFor(cardIdsCsv: String): Int {
    val ids = cardIdsCsv.split(',').filter { it.isNotBlank() }.toSet()
    return when {
        ids.any { it in WHIZBANG_DBF_IDS } -> 1
        ids.any { it in PRINCE_RENATHAL_DBF_IDS } -> 40
        else -> 30
    }
}

private fun SavedDeckEntity.formatFromCode(): GameFormat =
    runCatching { Deckstring.decode(code).format.toGameFormat() }
        .getOrElse { GameFormat.fromApi(format) }

private fun DeckstringFormat.toGameFormat(): GameFormat = when (this) {
    DeckstringFormat.WILD -> GameFormat.WILD
    DeckstringFormat.STANDARD -> GameFormat.STANDARD
    DeckstringFormat.CLASSIC -> GameFormat.CLASSIC
    DeckstringFormat.TWIST -> GameFormat.TWIST
}

private val PRINCE_RENATHAL_DBF_IDS = setOf("79767", "111689")
private val WHIZBANG_DBF_IDS = setOf("50477", "104819")
private const val MAX_DECK_NAME_LENGTH = 100
