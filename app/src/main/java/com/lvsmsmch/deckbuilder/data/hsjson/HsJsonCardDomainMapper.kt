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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ART_BASE = "https://art.hearthstonejson.com/v1"
/** 256x is the smallest official render size — keeps grid scrolling fast. */
private const val THUMB_SIZE = "256x"
/** Detail screen swaps in the 512x render via [renderUrl]. */
private const val FULL_SIZE = "512x"
private val UnknownType = CardType(id = 0, slug = "unknown", name = "")
private val PayloadJson = Json { ignoreUnknownKeys = true }

/** Builds a render URL at the given size for [cardId] in [locale]. */
fun renderUrl(cardId: String, locale: String, size: String = FULL_SIZE): String =
    "$ART_BASE/render/latest/$locale/$size/$cardId.png"

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
    val (artist, flavor) = parseArtistAndFlavor(payloadJson)

    return Card(
        id = dbfId,
        slug = cardId,
        name = name,
        text = text?.takeUnless { it.isBlank() },
        flavorText = flavor,
        // Grid thumbnails use 256x — ~3x smaller payload than 512x. Detail
        // screen swaps to FULL_SIZE via [renderUrl].
        image = renderUrl(cardId, locale, THUMB_SIZE),
        cropImage = "$ART_BASE/tiles/$cardId.png",
        artistName = artist,
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

/**
 * Pulls `artist` and `flavor` out of the stored HsJson payload. We don't
 * promote these to entity columns because they're only ever read on the
 * card-detail screen — parsing on demand is cheap (only paged slices ever
 * call [toDomain]).
 */
private fun parseArtistAndFlavor(payloadJson: String): Pair<String?, String?> {
    if (payloadJson.isBlank()) return null to null
    return runCatching {
        val obj = PayloadJson.parseToJsonElement(payloadJson).jsonObject
        val artist = obj["artist"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        val flavor = obj["flavor"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        artist to flavor
    }.getOrElse { null to null }
}
