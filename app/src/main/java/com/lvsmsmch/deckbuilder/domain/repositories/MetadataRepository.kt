package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import kotlinx.coroutines.flow.StateFlow

interface MetadataRepository {

    val current: StateFlow<Metadata?>

    /** Loads cache from Room into [current]. No network. Resolves to current pref locale when null. */
    suspend fun loadFromCache(locale: String? = null): Metadata?

    /**
     * Hits `GET /hearthstone/metadata?locale=...`, persists to Room, updates
     * [current]. With [force] = false, becomes a no-op when cache is fresh.
     */
    suspend fun refresh(locale: String? = null, force: Boolean = false): Result<Metadata>
}
