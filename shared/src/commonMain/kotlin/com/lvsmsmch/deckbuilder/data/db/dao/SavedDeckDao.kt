package com.lvsmsmch.deckbuilder.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lvsmsmch.deckbuilder.data.db.entity.SavedDeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDeckDao {

    @Query("SELECT * FROM saved_decks ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<SavedDeckEntity>>

    @Query("SELECT * FROM saved_decks WHERE code = :code LIMIT 1")
    suspend fun get(code: String): SavedDeckEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_decks WHERE code = :code)")
    suspend fun exists(code: String): Boolean

    @Upsert
    suspend fun upsert(entity: SavedDeckEntity)

    @Query("DELETE FROM saved_decks WHERE code = :code")
    suspend fun delete(code: String)

    @Query("UPDATE saved_decks SET name = :name, updatedAtMs = :updatedAtMs WHERE code = :code")
    suspend fun rename(code: String, name: String, updatedAtMs: Long)
}
