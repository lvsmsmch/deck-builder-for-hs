package com.lvsmsmch.deckbuilder.data.repository

import com.lvsmsmch.deckbuilder.util.AppLog
import com.lvsmsmch.deckbuilder.data.db.dao.SavedDeckDao
import com.lvsmsmch.deckbuilder.data.db.entity.SavedDeckEntity
import com.lvsmsmch.deckbuilder.data.deckstring.Deckstring
import com.lvsmsmch.deckbuilder.data.deckstring.DeckstringFormat
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckRules
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.SavedDeckSource
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import com.lvsmsmch.deckbuilder.util.IoDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

private const val TAG = "DB.SavedDeckRepo"

/**
 * [manaCostsOf] resolves dbfIds to mana costs for the list's deck curves. It is
 * a plain function rather than the card repository so this class keeps its one
 * real dependency — the saved-deck table — and defaults to no curves.
 */
class SavedDeckRepositoryImpl(
    private val dao: SavedDeckDao,
    private val manaCostsOf: suspend (Collection<Int>) -> Map<Int, Int> = { emptyMap() },
    private val nowMs: () -> Long = { kotlinx.datetime.Clock.System.now().toEpochMilliseconds() },
) : SavedDeckRepository {

    override fun observeAll(): Flow<List<DeckPreview>> =
        dao.observeAll()
            .onEach { rows -> AppLog.d(TAG, "observeAll: emit ${rows.size} rows") }
            .map { rows -> rows.map { row -> toPreview(row, curvesFor(rows)[row.code].orEmpty()) } }
            .flowOn(IoDispatcher)

    /**
     * Mana curves for every listed deck in one query: the ids of all decks are
     * looked up together, then each deck's curve is counted from that map.
     * Falls back to empty curves when card data has not been downloaded yet.
     */
    private suspend fun curvesFor(rows: List<SavedDeckEntity>): Map<String, List<Int>> {
        if (rows.isEmpty()) return emptyMap()
        val idsByDeck = rows.associate { it.code to it.cardIdsCsv.toCardIds() }
        val allIds = idsByDeck.values.flatten().toSet()
        val costs = runCatching { manaCostsOf(allIds) }.getOrElse { return emptyMap() }
        if (costs.isEmpty()) return emptyMap()
        return idsByDeck.mapValues { (_, ids) ->
            val buckets = MutableList(CURVE_BUCKETS) { 0 }
            ids.forEach { id ->
                val cost = costs[id] ?: return@forEach
                val index = cost.coerceIn(0, CURVE_BUCKETS - 1)
                buckets[index] = buckets[index] + 1
            }
            if (buckets.all { it == 0 }) emptyList() else buckets
        }
    }

    override suspend fun isSaved(code: String): Boolean = withContext(IoDispatcher) {
        val exists = dao.exists(code)
        AppLog.d(TAG, "isSaved: code='${code.take(12)}…' → $exists")
        exists
    }

    override suspend fun save(deck: Deck, name: String?): Unit = withContext(IoDispatcher) {
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
            AppLog.i(TAG, "save: OK code='${deck.code.take(12)}…' name='$resolvedName' cards=${deck.cardCount} ${if (existing == null) "[new]" else "[update]"}")
        } catch (t: Throwable) {
            AppLog.w(TAG, "save: FAILED code='${deck.code.take(12)}…': ${t.message}", t)
            throw t
        }
    }

    override suspend fun delete(code: String): Unit = withContext(IoDispatcher) {
        try {
            dao.delete(code)
            AppLog.i(TAG, "delete: OK code='${code.take(12)}…'")
        } catch (t: Throwable) {
            AppLog.w(TAG, "delete: FAILED code='${code.take(12)}…': ${t.message}", t)
            throw t
        }
    }

    override suspend fun rename(code: String, name: String): Unit = withContext(IoDispatcher) {
        require(code.isNotBlank()) { "Deck code is empty — cannot rename" }
        val trimmed = name.trim().take(MAX_DECK_NAME_LENGTH)
        require(trimmed.isNotEmpty()) { "Deck name cannot be empty" }
        dao.rename(code, trimmed, nowMs())
        AppLog.i(TAG, "rename: OK code='${code.take(12)}…' → '$trimmed'")
    }

    override suspend fun get(code: String): DeckPreview? = withContext(IoDispatcher) {
        dao.get(code)?.let(::toPreview)
    }

    override suspend fun getSource(code: String): SavedDeckSource? = withContext(IoDispatcher) {
        dao.get(code)?.let { row ->
            SavedDeckSource(
                code = row.code,
                name = row.name,
                heroCardId = row.heroCardId.takeIf { it > 0 },
                format = GameFormat.fromApi(row.format),
                cardIds = row.cardIdsCsv.toCardIds(),
            )
        }
    }

    private fun toPreview(row: SavedDeckEntity, manaCurve: List<Int> = emptyList()): DeckPreview = DeckPreview(
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
        manaCurve = manaCurve,
    )

    private fun defaultName(deck: Deck): String {
        val cls = deck.heroClass?.name?.ifBlank { null }
        return cls?.let { "$it deck" } ?: "Untitled deck"
    }
}

private fun String.toCardIds(): List<Int> =
    split(',').mapNotNull { it.trim().toIntOrNull() }

private fun maxCardCountFor(cardIdsCsv: String): Int =
    DeckRules.maxCardCountForDbfIds(cardIdsCsv.toCardIds())

private fun SavedDeckEntity.formatFromCode(): GameFormat =
    runCatching { Deckstring.decode(code).format.toGameFormat() }
        .getOrElse { GameFormat.fromApi(format) }

private fun DeckstringFormat.toGameFormat(): GameFormat = when (this) {
    DeckstringFormat.WILD -> GameFormat.WILD
    DeckstringFormat.STANDARD -> GameFormat.STANDARD
    DeckstringFormat.CLASSIC -> GameFormat.CLASSIC
    DeckstringFormat.TWIST -> GameFormat.TWIST
}

private const val MAX_DECK_NAME_LENGTH = 100
private const val CURVE_BUCKETS = 8
