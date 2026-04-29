package com.lvsmsmch.deckbuilder.data.hsjson.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Single row from HearthstoneJSON `cards.collectible.json`.
 * The schema is documented at https://hearthstonejson.com/docs/cards.html
 * Fields that we don't currently use are left out — kotlinx-serialization
 * is configured with [ignoreUnknownKeys].
 */
@Serializable
data class HsJsonCardDto(
    val id: String,
    val dbfId: Int,
    val name: String? = null,
    val text: String? = null,
    val flavor: String? = null,
    val cost: Int? = null,
    val attack: Int? = null,
    val health: Int? = null,
    val durability: Int? = null,
    val armor: Int? = null,
    val cardClass: String? = null,
    val classes: List<String>? = null,
    val multiClassGroup: String? = null,
    @SerialName("set") val cardSet: String? = null,
    val type: String? = null,
    val rarity: String? = null,
    val race: String? = null,
    val races: List<String>? = null,
    val spellSchool: String? = null,
    val mechanics: List<String>? = null,
    val referencedTags: List<String>? = null,
    val collectible: Boolean? = null,
    val elite: Boolean? = null,
    val artist: String? = null,
    val faction: String? = null,
    val howToEarn: String? = null,
    val howToEarnGolden: String? = null,
    val techLevel: Int? = null,
)
