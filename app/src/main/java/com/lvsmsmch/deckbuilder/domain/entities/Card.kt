package com.lvsmsmch.deckbuilder.domain.entities

data class Card(
    val id: Int,
    val slug: String,
    val name: String,
    val text: String?,
    val flavorText: String?,
    val image: String,
    val cropImage: String?,
    val artistName: String?,
    val manaCost: Int,
    val attack: Int?,
    val health: Int?,
    val durability: Int?,
    val armor: Int?,
    val classes: List<ClassMeta>,
    val cardSet: Expansion?,
    val rarity: Rarity?,
    val cardType: CardType,
    val minionType: MinionType?,
    val spellSchool: SpellSchool?,
    val keywords: List<Keyword>,
    val collectible: Boolean,
    val childIds: List<Int>,
    val battlegrounds: BattlegroundsMeta? = null,
)

data class BattlegroundsMeta(
    val tier: Int?,
    val isHero: Boolean,
    val upgradeId: Int?,
)

data class ClassMeta(
    val id: Int,
    val slug: String,
    val name: String,
    /** Primary hero card id, used by the deck builder to lock class via the `hero` API param. */
    val heroCardId: Int? = null,
)

data class Expansion(
    val id: Int,
    val slug: String,
    val name: String,
    val type: String?,
)

data class Rarity(
    val id: Int,
    val slug: String,
    val name: String,
    val craftingCost: List<Int>,
)

data class CardType(
    val id: Int,
    val slug: String,
    val name: String,
)

data class MinionType(
    val id: Int,
    val slug: String,
    val name: String,
)

data class SpellSchool(
    val id: Int,
    val slug: String,
    val name: String,
)

data class Keyword(
    val id: Int,
    val slug: String,
    val name: String,
    val refText: String,
)
