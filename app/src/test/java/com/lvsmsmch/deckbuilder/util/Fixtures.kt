package com.lvsmsmch.deckbuilder.util

import com.lvsmsmch.deckbuilder.data.network.dto.CardDto
import com.lvsmsmch.deckbuilder.data.network.dto.CardSearchResponseDto
import com.lvsmsmch.deckbuilder.data.network.dto.ClassDto
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import com.lvsmsmch.deckbuilder.data.network.dto.RarityDto
import com.lvsmsmch.deckbuilder.data.network.dto.SetDto
import com.lvsmsmch.deckbuilder.data.network.dto.SetGroupDto
import com.lvsmsmch.deckbuilder.data.network.dto.TypeDto
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Expansion
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.domain.entities.SetGroup

/** Small but realistic [MetadataAllDto] used by API stubs. */
fun fakeMetadataDto(): MetadataAllDto = MetadataAllDto(
    sets = listOf(
        SetDto(id = 1, name = "Classic", slug = "classic", type = "expansion"),
        SetDto(id = 2, name = "Whispers", slug = "whispers", type = "expansion"),
    ),
    setGroups = listOf(
        SetGroupDto(slug = "standard", name = "Standard", standard = true, cardSets = listOf("classic")),
    ),
    types = listOf(
        TypeDto(id = 4, name = "Minion", slug = "minion"),
        TypeDto(id = 5, name = "Spell", slug = "spell"),
    ),
    rarities = listOf(
        RarityDto(id = 1, name = "Common", slug = "common"),
        RarityDto(id = 5, name = "Legendary", slug = "legendary"),
    ),
    classes = listOf(
        ClassDto(id = 2, name = "Druid", slug = "druid"),
        ClassDto(id = 3, name = "Hunter", slug = "hunter"),
        ClassDto(id = 4, name = "Mage", slug = "mage"),
    ),
)

/** Domain-level metadata snapshot for tests that bypass mappers. */
fun fakeMetadata(
    locale: String = "en_US",
    refreshedAtMs: Long = 1_000L,
): Metadata = Metadata(
    sets = mapOf(
        1 to Expansion(1, "classic", "Classic", "expansion"),
        2 to Expansion(2, "whispers", "Whispers", "expansion"),
    ),
    setGroups = mapOf(
        "standard" to SetGroup("standard", "Standard", null, true, listOf("classic")),
    ),
    cardTypes = mapOf(
        4 to CardType(4, "minion", "Minion"),
        5 to CardType(5, "spell", "Spell"),
    ),
    rarities = mapOf(
        1 to Rarity(1, "common", "Common", emptyList()),
        5 to Rarity(5, "legendary", "Legendary", emptyList()),
    ),
    classes = mapOf(
        2 to ClassMeta(2, "druid", "Druid"),
        3 to ClassMeta(3, "hunter", "Hunter"),
        4 to ClassMeta(4, "mage", "Mage"),
    ),
    minionTypes = emptyMap(),
    keywords = emptyMap(),
    spellSchools = emptyMap(),
    gameModes = emptyMap(),
    locale = locale,
    refreshedAtMs = refreshedAtMs,
)

fun fakeCardDto(
    id: Int = 1,
    name: String = "Test Card",
    classId: Int? = 2,
    rarityId: Int? = 1,
    cardTypeId: Int? = 4,
    cardSetId: Int? = 1,
    manaCost: Int = 3,
): CardDto = CardDto(
    id = id,
    slug = "card-$id",
    name = name,
    classId = classId,
    rarityId = rarityId,
    cardTypeId = cardTypeId,
    cardSetId = cardSetId,
    manaCost = manaCost,
    image = "https://example.test/$id.png",
)

fun fakeSearchResponse(
    items: List<CardDto> = listOf(fakeCardDto()),
    page: Int = 1,
    pageCount: Int = 1,
    cardCount: Int = items.size,
): CardSearchResponseDto =
    CardSearchResponseDto(cards = items, cardCount = cardCount, pageCount = pageCount, page = page)
