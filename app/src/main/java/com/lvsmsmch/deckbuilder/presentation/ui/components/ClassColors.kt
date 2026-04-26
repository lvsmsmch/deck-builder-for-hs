package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.ui.graphics.Color
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/** Maps a class slug onto the desaturated dark-theme palette from plan §8.1. */
fun colorForClassSlug(slug: String?): Color = when (slug?.lowercase()) {
    "druid" -> DeckBuilderColors.Class.Druid
    "hunter" -> DeckBuilderColors.Class.Hunter
    "mage" -> DeckBuilderColors.Class.Mage
    "paladin" -> DeckBuilderColors.Class.Paladin
    "priest" -> DeckBuilderColors.Class.Priest
    "rogue" -> DeckBuilderColors.Class.Rogue
    "shaman" -> DeckBuilderColors.Class.Shaman
    "warlock" -> DeckBuilderColors.Class.Warlock
    "warrior" -> DeckBuilderColors.Class.Warrior
    "demonhunter", "demon-hunter", "demon_hunter" -> DeckBuilderColors.Class.DemonHunter
    "deathknight", "death-knight", "death_knight" -> DeckBuilderColors.Class.DeathKnight
    else -> DeckBuilderColors.Class.Neutral
}

fun primaryClassColor(card: Card): Color = colorForClassSlug(card.classes.firstOrNull()?.slug)

fun classDisplayName(meta: ClassMeta?): String = meta?.name ?: "Neutral"

fun colorForRaritySlug(slug: String?): Color = when (slug?.lowercase()) {
    "rare" -> DeckBuilderColors.Rarity.Rare
    "epic" -> DeckBuilderColors.Rarity.Epic
    "legendary" -> DeckBuilderColors.Rarity.Legendary
    "common", "free" -> DeckBuilderColors.Rarity.Common
    else -> DeckBuilderColors.Rarity.Common
}

fun rarityColor(rarity: Rarity?): Color = colorForRaritySlug(rarity?.slug)
