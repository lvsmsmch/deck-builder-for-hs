package com.lvsmsmch.deckbuilder

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.Rarity

/** Minimal card factory for tests — only the fields assertions care about. */
fun testCard(
    id: Int,
    slug: String = "CARD_$id",
    name: String = "Card $id",
    text: String? = "text",
    typeSlug: String = "minion",
    manaCost: Int = 1,
    rarity: Rarity? = null,
): Card = Card(
    id = id,
    slug = slug,
    name = name,
    text = text,
    flavorText = null,
    image = "",
    cropImage = null,
    artistName = null,
    manaCost = manaCost,
    attack = null,
    health = null,
    durability = null,
    armor = null,
    classes = emptyList(),
    cardSet = null,
    rarity = rarity,
    cardType = CardType(0, typeSlug, typeSlug),
    minionType = null,
    spellSchool = null,
    keywords = emptyList(),
    collectible = true,
    childIds = emptyList(),
)
