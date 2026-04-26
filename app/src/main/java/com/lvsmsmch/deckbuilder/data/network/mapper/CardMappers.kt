package com.lvsmsmch.deckbuilder.data.network.mapper

import com.lvsmsmch.deckbuilder.data.network.dto.CardDto
import com.lvsmsmch.deckbuilder.domain.entities.BattlegroundsMeta
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Metadata

private val UnknownType = CardType(id = 0, slug = "unknown", name = "")

/**
 * Resolves IDs against [metadata]. Pass [Metadata.Empty] before metadata is
 * loaded — IDs that can't be resolved fall back to slug-less placeholders.
 */
fun CardDto.toDomain(metadata: Metadata): Card {
    val classes = buildList {
        classId?.let { metadata.classes[it]?.let(::add) }
        multiClassIds.forEach { id -> metadata.classes[id]?.let(::add) }
    }.distinctBy(ClassMeta::id)

    return Card(
        id = id,
        slug = slug,
        name = name,
        text = text?.takeUnless { it.isBlank() },
        flavorText = flavorText?.takeUnless { it.isBlank() },
        image = image,
        cropImage = cropImage,
        artistName = artistName,
        manaCost = manaCost,
        attack = attack,
        health = health,
        durability = durability,
        armor = armor,
        classes = classes,
        cardSet = cardSetId?.let { metadata.sets[it] },
        rarity = rarityId?.let { metadata.rarities[it] },
        cardType = cardTypeId?.let { metadata.cardTypes[it] } ?: UnknownType,
        minionType = minionTypeId?.let { metadata.minionTypes[it] },
        spellSchool = spellSchoolId?.let { metadata.spellSchools[it] },
        keywords = keywordIds.mapNotNull { metadata.keywords[it] },
        collectible = collectible == 1,
        childIds = childIds,
        battlegrounds = battlegrounds?.let {
            BattlegroundsMeta(tier = it.tier, isHero = it.hero, upgradeId = it.upgradeId)
        },
    )
}
