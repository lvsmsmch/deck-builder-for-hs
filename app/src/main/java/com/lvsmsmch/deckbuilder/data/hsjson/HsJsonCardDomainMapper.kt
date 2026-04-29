package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Expansion
import com.lvsmsmch.deckbuilder.domain.entities.Keyword
import com.lvsmsmch.deckbuilder.domain.entities.MinionType
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.domain.entities.SpellSchool

private const val ART_BASE = "https://art.hearthstonejson.com/v1"
private val UnknownType = CardType(id = 0, slug = "unknown", name = "")

/**
 * HsJson stores tokens like `MAGE`, `BLACK_TEMPLE`, `DEMONHUNTER`. We project
 * them to lowercase domain slugs so UI comparisons that came from the legacy
 * Blizzard metadata pipeline still work — at least for the simple cases.
 * Phase 6 will replace this with a hardcoded localized label table and drop
 * the ID fields entirely.
 */
internal fun HsJsonCardEntity.toDomain(): Card {
    val classTokens = parseClassTokens()
    val classes = classTokens.map { token ->
        ClassMeta(id = 0, slug = token.toDomainSlug(), name = token.toDisplayName())
    }
    val races = parseList(raceCsv)

    return Card(
        id = dbfId,
        slug = cardId,
        name = name,
        text = text?.takeUnless { it.isBlank() },
        flavorText = null,
        image = "$ART_BASE/render/latest/$locale/512x/$cardId.png",
        cropImage = "$ART_BASE/tiles/$cardId.png",
        artistName = null,
        manaCost = cost ?: 0,
        attack = attack,
        health = health,
        durability = durability,
        armor = armor,
        classes = classes,
        cardSet = cardSet?.let { Expansion(0, it.toDomainSlug(), it.toDisplayName(), null) },
        rarity = rarity?.let { Rarity(0, it.toDomainSlug(), it.toDisplayName(), emptyList()) },
        cardType = type?.let { CardType(0, it.toDomainSlug(), it.toDisplayName()) } ?: UnknownType,
        minionType = races.firstOrNull()
            ?.let { MinionType(0, it.toDomainSlug(), it.toDisplayName()) },
        spellSchool = spellSchool?.let {
            SpellSchool(0, it.toDomainSlug(), it.toDisplayName())
        },
        keywords = parseList(mechanicsCsv).map {
            Keyword(0, it.toDomainSlug(), it.toDisplayName(), refText = "")
        },
        collectible = collectible,
        childIds = emptyList(),
    )
}

internal fun HsJsonCardEntity.parseClassTokens(): List<String> {
    val multi = parseList(classesCsv)
    if (multi.isNotEmpty()) return multi
    return listOfNotNull(cardClass)
}

private fun parseList(csv: String?): List<String> =
    csv?.trim(',')?.takeIf { it.isNotBlank() }?.split(',') ?: emptyList()

/** "DEATH_KNIGHT" → "death-knight", "DEMONHUNTER" → "demonhunter". */
internal fun String.toDomainSlug(): String = lowercase().replace('_', '-')

private fun String.toDisplayName(): String = split('_', '-').joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { it.uppercaseChar() }
}
