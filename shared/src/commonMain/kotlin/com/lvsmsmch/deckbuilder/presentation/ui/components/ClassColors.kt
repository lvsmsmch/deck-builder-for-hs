package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Rarity
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/** The lighter stop of a class gradient, for the rare spot that needs one colour. */
fun colorForClassSlug(slug: String?): Color = DeckBuilderColors.Class.accent(slug)

/** Both stops, for art shards and hero bands. */
fun classGradient(slug: String?): Pair<Color, Color> = DeckBuilderColors.Class.of(slug)

fun primaryClassColor(card: Card): Color = colorForClassSlug(card.classes.firstOrNull()?.slug)

fun cardGradient(card: Card): Pair<Color, Color> = classGradient(card.classes.firstOrNull()?.slug)

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
    GameFormat.STANDARD -> DeckBuilderColors.OnSurfaceDim
    GameFormat.WILD -> DeckBuilderColors.Secondary
    GameFormat.TWIST -> Color(0xFF9B6CFF)
    GameFormat.CLASSIC -> DeckBuilderColors.Success
    GameFormat.UNKNOWN -> DeckBuilderColors.OnSurfaceDimmer
}
