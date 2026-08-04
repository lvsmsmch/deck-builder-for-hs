package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/** Maps a class slug onto the app class palette. */
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

/**
 * Format accent. Standard rides the mana accent (the format most decks live
 * in), Wild takes gold; both come from the theme so they stay legible on
 * either ground.
 */
@Composable
@ReadOnlyComposable
fun formatColor(format: GameFormat): Color = when (format) {
    GameFormat.STANDARD -> DeckBuilderColors.Primary
    GameFormat.WILD -> DeckBuilderColors.Secondary
    GameFormat.TWIST -> Color(0xFF9B6CFF)
    GameFormat.CLASSIC -> DeckBuilderColors.Success
    GameFormat.UNKNOWN -> DeckBuilderColors.OnSurfaceDimmer
}
