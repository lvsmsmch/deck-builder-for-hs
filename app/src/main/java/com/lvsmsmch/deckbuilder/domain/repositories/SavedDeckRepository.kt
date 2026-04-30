package com.lvsmsmch.deckbuilder.domain.repositories

import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import kotlinx.coroutines.flow.Flow

interface SavedDeckRepository {

    fun observeAll(): Flow<List<DeckPreview>>

    suspend fun isSaved(code: String): Boolean

    suspend fun save(deck: Deck, name: String? = null)

    suspend fun delete(code: String)

    suspend fun rename(code: String, name: String)

    suspend fun get(code: String): DeckPreview?
}
