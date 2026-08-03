package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.util.AppLog
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.data.db.HiddenCardSets
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.data.update.CardDataProgress
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import com.lvsmsmch.deckbuilder.util.ConcurrentCache

private const val TAG = "DB.HsJson.Repo"

/**
 * Loads + caches HearthstoneJSON `cards.json` per locale.
 *
 * - First call for a locale fetches the latest build, stores it, and remembers
 *   the resolved build number.
 * - Subsequent calls hit Room.
 * - [checkForUpdate] polls the latest build and re-fetches if it changed.
 *
 * The repo accepts app locale codes (`en_US`) and converts them to the
 * HearthstoneJSON URL form internally.
 */
class HsJsonRepository(
    private val api: HsJsonApi,
    private val buildChecker: BuildChecker,
    private val dao: HsJsonCardDao,
    private val builds: HsJsonBuildStore,
    private val json: Json,
    private val sessionLog: SessionLog,
    private val notifier: UpdateNotifier,
) {
    private val mutex = Mutex()
    private val cachedBuilds = ConcurrentCache<String, String>()

    /** True once the locale has a complete card dataset stored. */
    suspend fun hasCards(appLocale: String): Boolean {
        val hs = appLocaleToHsJson(appLocale)
        return dao.count(hs) > 0 && builds.hasFullCardsDataset(hs)
    }

    /**
     * Sets that appear on collectible cards — input for the rotation
     * cross-check. Sets the app hides are left out: they are duplicates and
     * internal buckets, not releases python-hearthstone could ever know about.
     */
    suspend fun collectibleSets(appLocale: String): Set<String> =
        dao.collectibleSets(appLocaleToHsJson(appLocale))
            .filterNot { HiddenCardSets.isHidden(it) }
            .toSet()

    /**
     * Makes sure the locale has card data, downloading it when missing, and
     * returns the HearthstoneJSON locale to query with. Nothing is held in
     * memory: callers query Room directly.
     */
    suspend fun ensureLoaded(appLocale: String): String {
        val hs = appLocaleToHsJson(appLocale)
        if (dao.count(hs) > 0 && builds.hasFullCardsDataset(hs)) {
            builds.get(hs)?.let { cachedBuilds[hs] = it }
            return hs
        }
        return mutex.withLock {
            if (dao.count(hs) > 0 && builds.hasFullCardsDataset(hs)) {
                builds.get(hs)?.let { cachedBuilds[hs] = it }
                hs
            } else {
                fetchAndStore(hs = hs, reason = "ensureLoaded")
                hs
            }
        }
    }

    /**
     * Targeted lookup for deck assembly: a WHERE dbfId IN (...) query instead
     * of materialising the entire table. Triggers a full fetch only when the
     * locale has no data at all.
     */
    suspend fun cardsByDbfIds(appLocale: String, dbfIds: Collection<Int>): List<HsJsonCardEntity> {
        if (dbfIds.isEmpty()) return emptyList()
        val hs = ensureLoaded(appLocale)
        return dao.byDbfIds(hs, dbfIds.toList())
    }

    /**
     * If a newer build is available, replaces the cache for [appLocale].
     * Returns the new build number when an update was applied, null otherwise.
     */
    suspend fun checkForUpdate(appLocale: String): String? = mutex.withLock {
        val hs = appLocaleToHsJson(appLocale)
        val current = builds.get(hs)
        val latest = buildChecker.latestBuild(hs) ?: return null
        if (latest == current && builds.hasFullCardsDataset(hs)) {
            cachedBuilds[hs] = latest
            return null
        }
        AppLog.i(TAG, "checkForUpdate: $hs $current -> $latest")
        fetchAndStore(hs = hs, build = latest, reason = "checkForUpdate")
        latest
    }

    suspend fun currentBuild(appLocale: String): String? {
        val hs = appLocaleToHsJson(appLocale)
        return builds.get(hs)?.also { cachedBuilds[hs] = it }
    }

    /**
     * Card count + payload size for the Card data screen. Served from the
     * build store; falls back to SQL aggregates (no row materialisation) for
     * datasets stored before stats were recorded, then backfills the store.
     */
    suspend fun cachedStats(appLocale: String): HsJsonBuildStore.CardStats? {
        val hs = appLocaleToHsJson(appLocale)
        builds.stats(hs)?.let { return it }
        val count = dao.count(hs)
        if (count == 0) return null
        val bytes = dao.payloadChars(hs)
        builds.setStats(hs, count, bytes)
        return HsJsonBuildStore.CardStats(count, bytes)
    }

    fun cachedBuild(appLocale: String): String? =
        cachedBuilds[appLocaleToHsJson(appLocale)]

    private suspend fun fetchAndStore(
        hs: String,
        build: String? = null,
        reason: String,
    ) {
        try {
            notifier.setCardDataProgress(CardDataProgress(CardDataProgress.Stage.RESOLVING_BUILD))
            val resolvedBuild = build ?: buildChecker.latestBuild(hs)
                ?: error("HsJson: cannot resolve latest build for $hs")
            AppLog.i(TAG, "$reason: fetching build=$resolvedBuild locale=$hs")
            notifier.setCardDataProgress(CardDataProgress(CardDataProgress.Stage.DOWNLOADING))
            val dtos = api.cardsForBuild(resolvedBuild, hs)
            notifier.setCardDataProgress(CardDataProgress(CardDataProgress.Stage.PARSING))
            val rows = dtos.map { it.toEntity(hs, json) }
            notifier.setCardDataProgress(CardDataProgress(CardDataProgress.Stage.SAVING))
            dao.replaceLocale(hs, rows)
            builds.set(hs, resolvedBuild)
            builds.setStats(hs, rows.size, rows.sumOf { it.payloadJson.length.toLong() })
            cachedBuilds[hs] = resolvedBuild
            notifier.setCardDataProgress(null)
            AppLog.i(TAG, "$reason: stored ${rows.size} cards build=$resolvedBuild locale=$hs")
            sessionLog.add(TAG, "$reason locale=$hs build=$resolvedBuild cards=${rows.size}")
            return
        } catch (t: Throwable) {
            notifier.setCardDataProgress(null)
            throw t
        }
    }
}
