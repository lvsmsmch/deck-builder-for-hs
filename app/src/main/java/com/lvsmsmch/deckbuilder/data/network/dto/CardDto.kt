package com.lvsmsmch.deckbuilder.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    val id: Int,
    val collectible: Int = 1,
    val slug: String,
    @SerialName("classId") val classId: Int? = null,
    @SerialName("multiClassIds") val multiClassIds: List<Int> = emptyList(),
    @SerialName("cardTypeId") val cardTypeId: Int? = null,
    @SerialName("cardSetId") val cardSetId: Int? = null,
    @SerialName("rarityId") val rarityId: Int? = null,
    @SerialName("minionTypeId") val minionTypeId: Int? = null,
    @SerialName("spellSchoolId") val spellSchoolId: Int? = null,
    @SerialName("keywordIds") val keywordIds: List<Int> = emptyList(),
    @SerialName("manaCost") val manaCost: Int = 0,
    val attack: Int? = null,
    val health: Int? = null,
    val durability: Int? = null,
    val armor: Int? = null,
    val name: String = "",
    val text: String? = null,
    @SerialName("flavorText") val flavorText: String? = null,
    val image: String = "",
    @SerialName("imageGold") val imageGold: String? = null,
    @SerialName("cropImage") val cropImage: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("childIds") val childIds: List<Int> = emptyList(),
    val battlegrounds: BattlegroundsDto? = null,
)

@Serializable
data class BattlegroundsDto(
    val tier: Int? = null,
    val hero: Boolean = false,
    val upgradeId: Int? = null,
    @SerialName("companionId") val companionId: Int? = null,
)

@Serializable
data class CardSearchResponseDto(
    val cards: List<CardDto> = emptyList(),
    @SerialName("cardCount") val cardCount: Int = 0,
    @SerialName("pageCount") val pageCount: Int = 0,
    val page: Int = 1,
)
