package com.lvsmsmch.deckbuilder.domain.usecases

import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository

class RefreshMetadataUseCase(
    private val repo: MetadataRepository,
) {
    /**
     * Hydrate from Room first, then attempt a network refresh. The cache
     * load is best-effort; the network call returns the actual [Result].
     */
    suspend operator fun invoke(
        locale: String? = null,
        force: Boolean = false,
    ): Result<Metadata> {
        repo.loadFromCache(locale)
        return repo.refresh(locale = locale, force = force)
    }
}
