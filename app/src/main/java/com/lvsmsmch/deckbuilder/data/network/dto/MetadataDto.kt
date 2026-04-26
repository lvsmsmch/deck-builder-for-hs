package com.lvsmsmch.deckbuilder.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response shape of `GET /hearthstone/metadata?locale={locale}`.
 * When `locale` is supplied, every `name`/`text` field is a plain string.
 */
@Serializable
data class MetadataAllDto(
    val sets: List<SetDto> = emptyList(),
    val setGroups: List<SetGroupDto> = emptyList(),
    val types: List<TypeDto> = emptyList(),
    val rarities: List<RarityDto> = emptyList(),
    val classes: List<ClassDto> = emptyList(),
    val minionTypes: List<MinionTypeDto> = emptyList(),
    val keywords: List<KeywordDto> = emptyList(),
    val spellSchools: List<SpellSchoolDto> = emptyList(),
    val gameModes: List<GameModeDto> = emptyList(),
    val mercenaryRoles: List<MercenaryRoleDto> = emptyList(),
)

@Serializable
data class SetDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val type: String? = null,
    @SerialName("collectibleCount") val collectibleCount: Int? = null,
    @SerialName("collectibleRevealedCount") val collectibleRevealedCount: Int? = null,
    @SerialName("nonCollectibleCount") val nonCollectibleCount: Int? = null,
    @SerialName("nonCollectibleRevealedCount") val nonCollectibleRevealedCount: Int? = null,
    @SerialName("aliasSetIds") val aliasSetIds: List<Int> = emptyList(),
)

@Serializable
data class SetGroupDto(
    val slug: String,
    val name: String = "",
    val year: Int? = null,
    val standard: Boolean = false,
    val cardSets: List<String> = emptyList(),
    val yearRange: String? = null,
    val icon: String? = null,
    val svg: String? = null,
)

@Serializable
data class TypeDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val gameModes: List<Int> = emptyList(),
)

@Serializable
data class RarityDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val craftingCost: List<Int> = emptyList(),
    val dustValue: List<Int> = emptyList(),
)

@Serializable
data class ClassDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val cardId: Int? = null,
    val heroPowerCardId: Int? = null,
    val alternateHeroCardIds: List<Int> = emptyList(),
)

@Serializable
data class MinionTypeDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val gameModes: List<Int> = emptyList(),
)

@Serializable
data class KeywordDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val refText: String = "",
    val text: String = "",
    val gameModes: List<Int> = emptyList(),
)

@Serializable
data class SpellSchoolDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class GameModeDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
)

/** Mercenaries was sunset in 2024; we deserialize to keep parsing forgiving and ignore. */
@Serializable
data class MercenaryRoleDto(
    val id: Int = 0,
    val name: String = "",
    val slug: String = "",
)
