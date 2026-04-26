package com.lvsmsmch.deckbuilder.data.network.mapper

import com.lvsmsmch.deckbuilder.data.network.dto.ClassDto
import com.lvsmsmch.deckbuilder.data.network.dto.GameModeDto
import com.lvsmsmch.deckbuilder.data.network.dto.KeywordDto
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import com.lvsmsmch.deckbuilder.data.network.dto.MinionTypeDto
import com.lvsmsmch.deckbuilder.data.network.dto.RarityDto
import com.lvsmsmch.deckbuilder.data.network.dto.SetDto
import com.lvsmsmch.deckbuilder.data.network.dto.SetGroupDto
import com.lvsmsmch.deckbuilder.data.network.dto.SpellSchoolDto
import com.lvsmsmch.deckbuilder.data.network.dto.TypeDto
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Expansion
import com.lvsmsmch.deckbuilder.domain.entities.GameMode
import com.lvsmsmch.deckbuilder.domain.entities.Keyword
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.entities.MinionType
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.domain.entities.SetGroup
import com.lvsmsmch.deckbuilder.domain.entities.SpellSchool

fun MetadataAllDto.toDomain(locale: String, refreshedAtMs: Long): Metadata = Metadata(
    sets = sets.associate { it.id to it.toDomain() },
    setGroups = setGroups.associate { it.slug to it.toDomain() },
    cardTypes = types.associate { it.id to it.toDomain() },
    rarities = rarities.associate { it.id to it.toDomain() },
    classes = classes.associate { it.id to it.toDomain() },
    minionTypes = minionTypes.associate { it.id to it.toDomain() },
    keywords = keywords.associate { it.id to it.toDomain() },
    spellSchools = spellSchools.associate { it.id to it.toDomain() },
    gameModes = gameModes.associate { it.id to it.toDomain() },
    locale = locale,
    refreshedAtMs = refreshedAtMs,
)

private fun SetDto.toDomain() = Expansion(id = id, slug = slug, name = name, type = type)

private fun SetGroupDto.toDomain() = SetGroup(
    slug = slug, name = name, year = year, standard = standard, cardSets = cardSets,
)

private fun TypeDto.toDomain() = CardType(id = id, slug = slug, name = name)

private fun RarityDto.toDomain() =
    Rarity(id = id, slug = slug, name = name, craftingCost = craftingCost.filterNotNull())

private fun ClassDto.toDomain() = ClassMeta(id = id, slug = slug, name = name, heroCardId = cardId)

private fun MinionTypeDto.toDomain() = MinionType(id = id, slug = slug, name = name)

private fun KeywordDto.toDomain() =
    Keyword(id = id, slug = slug, name = name, refText = refText.ifBlank { text })

private fun SpellSchoolDto.toDomain() = SpellSchool(id = id, slug = slug, name = name)

private fun GameModeDto.toDomain() = GameMode(id = id, slug = slug, name = name)
