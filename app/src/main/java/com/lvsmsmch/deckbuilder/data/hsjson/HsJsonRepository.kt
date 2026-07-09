package com.lvsmsmch.deckbuilder.data.hsjson

import android.util.Log
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.data.update.CardDataProgress
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

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
    private val cachedBuilds = ConcurrentHashMap<String, String>()

    /**
     * Last materialised snapshot (one locale at a time). `dao.all()` pulls the
     * whole card table with full JSON payloads — seconds of work — so callers
     * on hot paths (deck assembly, search) must not trigger it repeatedly.
     */
    @Volatile
    private var snapshotCache: Snapshot? = null

    data class Snapshot(
        val locale: String,
        val build: String?,
        val cards: List<HsJsonCardEntity>,
    )

    /** Returns cached rows for the locale, or null if nothing has been fetched yet. */
    suspend fun cached(appLocale: String): Snapshot? {
        val hs = appLocaleToHsJson(appLocale)
        snapshotCache?.takeIf { it.locale == hs }?.let { return it }
        val rows = dao.all(hs)
        if (rows.isEmpty()) return null
        val build = builds.get(hs)
        build?.let { cachedBuilds[hs] = it }
        return Snapshot(locale = hs, build = build, cards = rows).also { snapshotCache = it }
    }

    /**
     * Ensures cards exist for [appLocale]. If empty, fetches latest build and
     * populates Room. Returns the snapshot (possibly fresh, possibly cached).
     */
    suspend fun ensureLoaded(appLocale: String): Snapshot {
        val hs = appLocaleToHsJson(appLocale)
        snapshotCache?.takeIf { it.locale == hs }?.let { return it }
        return mutex.withLock {
            snapshotCache?.takeIf { it.locale == hs }?.let { return it }
            val existing = dao.all(hs)
            if (existing.isNotEmpty() && builds.hasFullCardsDataset(hs)) {
                sessionLog.add(TAG, "cache hit locale=$hs cards=${existing.size}")
                val build = builds.get(hs)
                build?.let { cachedBuilds[hs] = it }
                Snapshot(hs, build, existing).also { snapshotCache = it }
            } else {
                fetchAndStore(hs = hs, reason = "ensureLoaded")
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
        val hs = appLocaleToHsJson(appLocale)
        snapshotCache?.takeIf { it.locale == hs }?.let { snap ->
            val wanted = dbfIds.toSet()
            return snap.cards.filter { it.dbfId in wanted }
        }
        if (dao.count(hs) > 0) return dao.byDbfIds(hs, dbfIds.toList())
        ensureLoaded(appLocale)
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
        Log.i(TAG, "checkForUpdate: $hs $current -> $latest")
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
    ): Snapshot {
        try {
            notifier.setCardDataProgress(CardDataProgress(CardDataProgress.Stage.RESOLVING_BUILD))
            val resolvedBuild = build ?: buildChecker.latestBuild(hs)
                ?: error("HsJson: cannot resolve latest build for $hs")
            Log.i(TAG, "$reason: fetching build=$resolvedBuild locale=$hs")
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
            Log.i(TAG, "$reason: stored ${rows.size} cards build=$resolvedBuild locale=$hs")
            sessionLog.add(TAG, "$reason locale=$hs build=$resolvedBuild cards=${rows.size}")
            return Snapshot(hs, resolvedBuild, rows).also { snapshotCache = it }
        } catch (t: Throwable) {
            notifier.setCardDataProgress(null)
            throw t
        }
    }
}
