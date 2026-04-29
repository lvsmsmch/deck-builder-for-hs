package com.lvsmsmch.deckbuilder.data.rotation

import android.util.Log
import com.lvsmsmch.deckbuilder.domain.entities.RotationStatus
import com.lvsmsmch.deckbuilder.domain.entities.StandardRotation
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "DB.Rotation.Repo"

class RotationRepositoryImpl(
    private val api: RotationApi,
    private val store: RotationStore,
    private val now: () -> Long = System::currentTimeMillis,
) : RotationRepository {

    private val mutex = Mutex()

    override suspend fun cached(): StandardRotation? = store.get()

    override suspend fun ensureLoaded(): StandardRotation = mutex.withLock {
        store.get()?.let { return@withLock it }
        fetchAndStore() ?: error("Rotation: cannot load enums.py")
    }

    override suspend fun refresh(): StandardRotation? = mutex.withLock {
        fetchAndStore()
    }

    override fun status(rotation: StandardRotation, collectibleSets: Set<String>): RotationStatus {
        // Sets that appear on collectible cards but aren't recognised by enums.py at all.
        val unknown = collectibleSets - rotation.knownSets
        return RotationStatus(rotation = rotation, unknownSets = unknown)
    }

    private suspend fun fetchAndStore(): StandardRotation? {
        val source = api.fetchEnumsSource() ?: return null
        val standard = EnumsParser.parseStandardSets(source)
        if (standard.isEmpty()) {
            Log.w(TAG, "fetchAndStore: STANDARD_SETS parsed empty — refusing to overwrite cache")
            return null
        }
        val known = EnumsParser.parseCardSetEnum(source)
        val commit = api.fetchLatestCommit()
        val rotation = StandardRotation(
            standardSets = standard,
            knownSets = known,
            sourceSha = commit?.sha,
            sourceCommittedAtIso = commit?.committedAtIso,
            fetchedAtMs = now(),
        )
        store.put(rotation)
        Log.i(
            TAG,
            "fetchAndStore: standard=${standard.size} known=${known.size} sha=${commit?.sha?.take(8)}",
        )
        return rotation
    }
}
