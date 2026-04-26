package com.lvsmsmch.deckbuilder.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the entire `/hearthstone/metadata?locale={locale}` payload as JSON.
 * One row per locale. Lookups happen against the in-memory [Metadata] domain
 * snapshot the repository builds — Room is just durable cache.
 */
@Entity(tableName = "metadata_blob")
data class MetadataBlobEntity(
    @PrimaryKey val locale: String,
    val payloadJson: String,
    val refreshedAtMs: Long,
)
