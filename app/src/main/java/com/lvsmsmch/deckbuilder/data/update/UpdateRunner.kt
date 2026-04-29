package com.lvsmsmch.deckbuilder.data.update

import android.util.Log
import com.lvsmsmch.deckbuilder.data.crash.CrashReporter
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository

private const val TAG = "DB.UpdateRunner"

/**
 * Single entry-point for "go check what's new" — used both by the app-start
 * kick-off in [com.lvsmsmch.deckbuilder.DeckBuilderApp] and by the daily
 * [UpdateWorker]. Posts results through [UpdateNotifier] for UI surfaces.
 */
class UpdateRunner(
    private val prefs: PreferencesRepository,
    private val hsJson: HsJsonRepository,
    private val rotation: RotationRepository,
    private val notifier: UpdateNotifier,
    private val crash: CrashReporter,
    private val now: () -> Long = System::currentTimeMillis,
) {

    /** Returns true if anything (cards or rotation) was actually applied. */
    suspend fun runOnce(reason: String): Boolean {
        Log.i(TAG, "runOnce: $reason")
        var applied = false

        val locale = runCatching { prefs.current().cardLocale }.getOrDefault("en_US")

        runCatching { hsJson.ensureLoaded(locale) }
            .onFailure {
                Log.w(TAG, "ensureLoaded failed: ${it.message}", it)
                crash.log("HsJson ensureLoaded failed: ${it.message}")
            }

        runCatching { hsJson.checkForUpdate(locale) }
            .onSuccess { newBuild ->
                if (newBuild != null) {
                    applied = true
                    Log.i(TAG, "cards updated to build=$newBuild")
                    notifier.emit(UpdateEvent.CardsUpdated(newBuild))
                }
            }
            .onFailure {
                Log.w(TAG, "checkForUpdate failed: ${it.message}", it)
                crash.log("HsJson checkForUpdate failed: ${it.message}")
            }

        runCatching {
            val cached = rotation.cached()
            if (cached == null) {
                rotation.ensureLoaded()
            } else {
                val refreshed = rotation.refresh()
                if (refreshed != null && refreshed.sourceSha != cached.sourceSha) {
                    applied = true
                    notifier.emit(UpdateEvent.RotationUpdated(refreshed.sourceSha))
                }
                refreshed ?: cached
            }
        }.onFailure {
            Log.w(TAG, "rotation refresh failed: ${it.message}", it)
            crash.log("Rotation refresh failed: ${it.message}")
        }

        // Re-publish lag status against current snapshot.
        runCatching {
            val rot = rotation.cached() ?: return@runCatching
            val snap = hsJson.cached(locale) ?: return@runCatching
            val collectibleSets = snap.cards
                .asSequence()
                .filter { it.collectible }
                .mapNotNull { it.cardSet }
                .toSet()
            val status = rotation.status(rot, collectibleSets)
            notifier.setRotationStatus(status)
            if (status.isOutdated) {
                Log.i(TAG, "rotation outdated: unknown=${status.unknownSets.take(5)}…")
            }
        }.onFailure {
            Log.w(TAG, "rotation cross-check failed: ${it.message}", it)
        }

        runCatching { prefs.setLastUpdateCheckAt(now()) }
        return applied
    }
}
