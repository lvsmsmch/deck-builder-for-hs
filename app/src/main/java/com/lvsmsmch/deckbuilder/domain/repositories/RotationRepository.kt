package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.entities.RotationStatus
import com.lvsmsmch.deckbuilder.domain.entities.StandardRotation

interface RotationRepository {

    /** Last cached snapshot, or null if nothing was ever fetched. */
    suspend fun cached(): StandardRotation?

    /** Returns cached snapshot, fetching from network on first call. */
    suspend fun ensureLoaded(): StandardRotation

    /** Re-fetches from network and replaces the cache. Returns the new snapshot or null on failure. */
    suspend fun refresh(): StandardRotation?

    /** Cross-check rotation against the set of `cardSet` tokens seen on collectible cards. */
    fun status(rotation: StandardRotation, collectibleSets: Set<String>): RotationStatus
}
