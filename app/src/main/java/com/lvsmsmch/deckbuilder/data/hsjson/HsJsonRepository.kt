package com.lvsmsmch.deckbuilder.data.hsjson

import android.util.Log
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

private const val TAG = "DB.HsJson.Repo"

/**
 * Loads + caches HearthstoneJSON `cards.collectible.json` per locale.
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
) {
    private val mutex = Mutex()

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
        return Snapshot(locale = hs, build = builds.get(hs), cards = rows)
    }

    /**
     * Ensures cards exist for [blizzardLocale]. If empty, fetches latest build and
     * populates Room. Returns the snapshot (possibly fresh, possibly cached).
     */
    suspend fun ensureLoaded(blizzardLocale: String): Snapshot = mutex.withLock {
        val hs = blizzardLocaleToHsJson(blizzardLocale)
        val existing = dao.all(hs)
        if (existing.isNotEmpty()) {
            return Snapshot(hs, builds.get(hs), existing)
        }
        val build = buildChecker.latestBuild(hs)
            ?: error("HsJson: cannot resolve latest build for $hs")
        Log.i(TAG, "ensureLoaded: fetching build=$build locale=$hs")
        val dtos = api.cardsForBuild(build, hs)
        val rows = dtos.map { it.toEntity(hs, json) }
        dao.replaceLocale(hs, rows)
        builds.set(hs, build)
        Log.i(TAG, "ensureLoaded: stored ${rows.size} cards build=$build locale=$hs")
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
        if (latest == current) return null
        Log.i(TAG, "checkForUpdate: $hs $current → $latest")
        val dtos = api.cardsForBuild(latest, hs)
        val rows = dtos.map { it.toEntity(hs, json) }
        dao.replaceLocale(hs, rows)
        builds.set(hs, latest)
        latest
    }

    suspend fun currentBuild(blizzardLocale: String): String? =
        builds.get(blizzardLocaleToHsJson(blizzardLocale))
}
