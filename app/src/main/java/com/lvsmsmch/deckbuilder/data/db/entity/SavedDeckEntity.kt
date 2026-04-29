package com.lvsmsmch.deckbuilder.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_decks")
data class SavedDeckEntity(
    @PrimaryKey val code: String,
    val name: String,
    val classSlug: String?,
    val className: String?,
    val heroCardId: Int,
    val heroSlug: String?,
    val format: String,
    val cardCount: Int,
    val cardIdsCsv: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)
