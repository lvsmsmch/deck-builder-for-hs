package com.lvsmsmch.deckbuilder.domain.entities

/**
 * In-memory snapshot of every metadata category we care about.
 * Keys are IDs (or slug for [SetGroup], which has no id).
 */
data class Metadata(
    val sets: Map<Int, Expansion>,
    val setGroups: Map<String, SetGroup>,
    val cardTypes: Map<Int, CardType>,
    val rarities: Map<Int, Rarity>,
    val classes: Map<Int, ClassMeta>,
    val minionTypes: Map<Int, MinionType>,
    val keywords: Map<Int, Keyword>,
    val spellSchools: Map<Int, SpellSchool>,
    val gameModes: Map<Int, GameMode>,
    val locale: String,
    val refreshedAtMs: Long,
) {
    companion object {
        val Empty: Metadata = Metadata(
            sets = emptyMap(),
            setGroups = emptyMap(),
            cardTypes = emptyMap(),
            rarities = emptyMap(),
            classes = emptyMap(),
            minionTypes = emptyMap(),
            keywords = emptyMap(),
            spellSchools = emptyMap(),
            gameModes = emptyMap(),
            locale = "en_US",
            refreshedAtMs = 0L,
        )
    }
}

data class GameMode(
    val id: Int,
    val slug: String,
    val name: String,
)

data class SetGroup(
    val slug: String,
    val name: String,
    val year: Int?,
    val standard: Boolean,
    val cardSets: List<String>,
)

/** Convenience extension on [ClassMeta] holders — fully resolved primary hero metadata is in classes table. */
data class ClassMetaExt(
    val meta: ClassMeta,
    val cardId: Int?,
    val heroPowerCardId: Int?,
    val alternateHeroCardIds: List<Int>,
)
