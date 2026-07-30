package com.lvsmsmch.deckbuilder.data.db.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * One row per (locale, dbfId). Hot fields are normalised for in-memory filtering;
 * the rest of the HsJson row lives in [payloadJson] so we don't lose data when
 * HearthstoneJSON adds new fields. CSVs (`,A,B,`) include leading/trailing
 * commas so a `LIKE '%,X,%'` substring match is unambiguous.
 */
@Entity(
    tableName = "hsjson_cards",
    primaryKeys = ["locale", "dbfId"],
    indices = [
        Index(value = ["locale", "cardClass"]),
        Index(value = ["locale", "cardSet"]),
        Index(value = ["locale", "cost"]),
    ],
)
data class HsJsonCardEntity(
    val locale: String,
    val dbfId: Int,
    val cardId: String,
    val name: String,
    val text: String?,
    val cost: Int?,
    val attack: Int?,
    val health: Int?,
    val durability: Int?,
    val armor: Int?,
    val cardClass: String?,
    val classesCsv: String?,
    val multiClassGroup: String?,
    val cardSet: String?,
    val type: String?,
    val rarity: String?,
    val raceCsv: String?,
    val spellSchool: String?,
    val mechanicsCsv: String?,
    val collectible: Boolean,
    val payloadJson: String,
)
