package com.lvsmsmch.deckbuilder.data.db.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.lvsmsmch.deckbuilder.data.db.entity.MetadataBlobEntity

@Dao
interface MetadataDao {

    @Query("SELECT * FROM metadata_blob WHERE locale = :locale LIMIT 1")
    suspend fun getBlob(locale: String): MetadataBlobEntity?

    @Upsert
    suspend fun upsert(blob: MetadataBlobEntity)
}
