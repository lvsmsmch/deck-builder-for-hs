package com.lvsmsmch.deckbuilder.data.repository

import androidx.room.RoomRawQuery
import androidx.sqlite.SQLiteStatement
import com.lvsmsmch.deckbuilder.data.db.CardQuery
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.hsjson.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import com.lvsmsmch.deckbuilder.util.AppLog
import com.lvsmsmch.deckbuilder.util.ConcurrentCache
import com.lvsmsmch.deckbuilder.util.IoDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

private const val TAG = "DB.CardRepo"

/**
 * Card queries run in SQLite: filtering, sorting and paging are all done by
 * the database, so only one page of rows is ever materialized. The card table
 * holds tens of thousands of rows with a JSON payload each — loading it into
 * memory to filter it (as this repository used to) cost tens of megabytes.
 */
class CardRepositoryImpl(
    private val hsJson: HsJsonRepository,
    private val dao: HsJsonCardDao,
    private val locales: CurrentLocaleProvider,
    private val sessionLog: SessionLog,
    private val rotation: RotationRepository,
) : CardRepository {

    // Bounded: every viewed card was cached under three keys and never freed.
    private val memoryCache = ConcurrentCache<String, Card>(maxSize = 600)

    override fun cachedCard(idOrSlug: String): Card? = memoryCache[idOrSlug.lowercase()]

    override suspend fun getCard(idOrSlug: String, locale: String?): Result<Card> =
        withContext(IoDispatcher) {
            val started = Clock.System.now().toEpochMilliseconds()
            runCatchingResult {
                val hsLocale = hsJson.ensureLoaded(locales.resolve(locale))
                val dbfId = idOrSlug.toIntOrNull()
                val row = if (dbfId != null) {
                    dao.byDbfId(hsLocale, dbfId)
                } else {
                    dao.byCardId(hsLocale, idOrSlug) ?: dao.byName(hsLocale, idOrSlug)
                } ?: error("Card not found in HsJson pool: $idOrSlug")
                row.toDomain().also(::remember)
            }.also { r ->
                when (r) {
                    is Result.Success -> {
                        AppLog.i(TAG, "getCard: OK idOrSlug=$idOrSlug name='${r.data.name}'")
                        sessionLog.add(
                            TAG,
                            "getCard id=$idOrSlug name='${r.data.name}' ms=${elapsedSince(started)}",
                        )
                    }
                    is Result.Error -> {
                        AppLog.w(TAG, "getCard: FAILED idOrSlug=$idOrSlug: ${r.throwable.message}", r.throwable)
                        sessionLog.add(TAG, "getCard FAILED id=$idOrSlug error=${r.throwable.message}")
                    }
                }
            }
        }

    override suspend fun searchCards(
        filters: CardFilters,
        page: Int,
        pageSize: Int,
        locale: String?,
    ): Result<Page<Card>> = withContext(IoDispatcher) {
        val started = Clock.System.now().toEpochMilliseconds()
        runCatchingResult {
            val hsLocale = hsJson.ensureLoaded(locales.resolve(locale))
            val standardSets = if (filters.format != CardFormatFilter.ALL) {
                rotation.ensureLoaded().standardSets
            } else {
                emptySet()
            }

            val query = CardQuery.build(filters, hsLocale, standardSets)
            val total = dao.count(
                rawQuery("SELECT COUNT(*) FROM hsjson_cards WHERE ${query.where}", query.args),
            )
            val pageCount = if (pageSize > 0 && total > 0) (total + pageSize - 1) / pageSize else 1
            val offset = ((page - 1).coerceAtLeast(0)) * pageSize
            val rows = dao.search(
                rawQuery(
                    sql = "SELECT * FROM hsjson_cards WHERE ${query.where} " +
                        "ORDER BY ${query.orderBy} LIMIT ? OFFSET ?",
                    args = query.args + listOf(pageSize, offset),
                ),
            )

            Page(
                items = rows.map { it.toDomain().also(::remember) },
                pageNumber = page,
                pageCount = pageCount,
                totalCount = total,
            )
        }.also { r ->
            val summary = "page=$page " +
                "classes=${filters.classes} sets=${filters.sets.size} format=${filters.format} " +
                "rarities=${filters.rarities} mana=${filters.manaCosts} " +
                "q='${filters.textQuery}'"
            when (r) {
                is Result.Success -> AppLog.i(
                    TAG,
                    "searchCards: OK $summary → ${r.data.items.size}/${r.data.totalCount} items, " +
                        "pageCount=${r.data.pageCount}",
                )
                is Result.Error -> AppLog.w(TAG, "searchCards: FAILED $summary: ${r.throwable.message}", r.throwable)
            }
            sessionLog.add(
                TAG,
                "search $summary result=${(r as? Result.Success)?.data?.items?.size ?: "FAILED"} " +
                    "ms=${elapsedSince(started)}",
            )
        }
    }

    /** Binds [args] positionally; only the types the query builder produces. */
    private fun rawQuery(sql: String, args: List<Any>): RoomRawQuery =
        RoomRawQuery(sql) { statement: SQLiteStatement ->
            args.forEachIndexed { index, arg ->
                val position = index + 1
                when (arg) {
                    is Int -> statement.bindInt(position, arg)
                    is Long -> statement.bindLong(position, arg)
                    is Boolean -> statement.bindInt(position, if (arg) 1 else 0)
                    else -> statement.bindText(position, arg.toString())
                }
            }
        }

    private fun elapsedSince(startMs: Long): Long = Clock.System.now().toEpochMilliseconds() - startMs

    private fun remember(card: Card) {
        memoryCache[card.id.toString()] = card
        memoryCache[card.slug.lowercase()] = card
        memoryCache[card.name.lowercase()] = card
    }
}
