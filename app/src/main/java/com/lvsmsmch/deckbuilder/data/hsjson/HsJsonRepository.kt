package com.lvsmsmch.deckbuilder.data.hsjson

import android.util.Log
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
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
 * The repo accepts Blizzard-style locales (`en_US`) for symmetry with the
 * existing prefs/metadata pipeline; internally it converts to the HsJson form.
 */
class HsJsonRepository(
    private val api: HsJsonApi,
    private val buildChecker: BuildChecker,
    private val dao: HsJsonCardDao,
    private val builds: HsJsonBuildStore,
    private val json: Json,
    private val sessionLog: SessionLog,
) {
    private val mutex = Mutex()
    private val cachedBuilds = ConcurrentHashMap<String, String>()

    data class Snapshot(
        val locale: String,
        val build: String?,
        val cards: List<HsJsonCardEntity>,
    )

    /** Returns cached rows for the locale, or null if nothing has been fetched yet. */
    suspend fun cached(blizzardLocale: String): Snapshot? {
        val hs = blizzardLocaleToHsJson(blizzardLocale)
        val rows = dao.all(hs)
        if (rows.isEmpty()) return null
        val build = builds.get(hs)
        build?.let { cachedBuilds[hs] = it }
        return Snapshot(locale = hs, build = build, cards = rows)
    }

    /**
     * Ensures cards exist for [blizzardLocale]. If empty, fetches latest build and
     * populates Room. Returns the snapshot (possibly fresh, possibly cached).
     */
    suspend fun ensureLoaded(blizzardLocale: String): Snapshot = mutex.withLock {
        val hs = blizzardLocaleToHsJson(blizzardLocale)
        val existing = dao.all(hs)
        if (existing.isNotEmpty() && builds.hasFullCardsDataset(hs)) {
            sessionLog.add(TAG, "cache hit locale=$hs cards=${existing.size}")
            val build = builds.get(hs)
            build?.let { cachedBuilds[hs] = it }
            return Snapshot(hs, build, existing)
        }
        val build = buildChecker.latestBuild(hs)
            ?: error("HsJson: cannot resolve latest build for $hs")
        Log.i(TAG, "ensureLoaded: fetching build=$build locale=$hs")
        val dtos = api.cardsForBuild(build, hs)
        val rows = dtos.map { it.toEntity(hs, json) }
        dao.replaceLocale(hs, rows)
        builds.set(hs, build)
        cachedBuilds[hs] = build
        Log.i(TAG, "ensureLoaded: stored ${rows.size} cards build=$build locale=$hs")
        sessionLog.add(TAG, "fetched locale=$hs build=$build cards=${rows.size}")
        Snapshot(hs, build, rows)
    }

    /**
     * If a newer build is available, replaces the cache for [blizzardLocale].
     * Returns the new build number when an update was applied, null otherwise.
     */
    suspend fun checkForUpdate(blizzardLocale: String): String? = mutex.withLock {
        val hs = blizzardLocaleToHsJson(blizzardLocale)
        val current = builds.get(hs)
        val latest = buildChecker.latestBuild(hs) ?: return null
        if (latest == current && builds.hasFullCardsDataset(hs)) {
            cachedBuilds[hs] = latest
            return null
        }
        Log.i(TAG, "checkForUpdate: $hs $current → $latest")
        val dtos = api.cardsForBuild(latest, hs)
        val rows = dtos.map { it.toEntity(hs, json) }
        dao.replaceLocale(hs, rows)
        builds.set(hs, latest)
        cachedBuilds[hs] = latest
        sessionLog.add(TAG, "updated locale=$hs build=$latest cards=${rows.size}")
        latest
    }

    suspend fun currentBuild(blizzardLocale: String): String? {
        val hs = blizzardLocaleToHsJson(blizzardLocale)
        return builds.get(hs)?.also { cachedBuilds[hs] = it }
    }

    fun cachedBuild(blizzardLocale: String): String? =
        cachedBuilds[blizzardLocaleToHsJson(blizzardLocale)]
}
