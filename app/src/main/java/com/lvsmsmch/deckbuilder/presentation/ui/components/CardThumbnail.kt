package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun CardThumbnail(
    card: Card,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val classColor = remember(card.id) { primaryClassColor(card) }
    val rarityColor = remember(card.id) { rarityColor(card.rarity) }
    val isLegendary = card.rarity?.slug?.equals("legendary", ignoreCase = true) == true

    // Hearthstone's full-card render is portrait (~0.72 aspect) and already shows
    // mana cost, name, attack/health, and rarity gem. We just frame it with a
    // rarity-coloured border. Earlier this used cropImage (a landscape art-only
    // crop) inside a portrait box with ContentScale.Crop, which zoomed in
    // dramatically and hid most of the art.
    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(classColor, DeckBuilderColors.Surface),
                ),
            )
            .border(
                width = if (isLegendary) 1.5.dp else 1.dp,
                color = rarityColor,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick),
    ) {
        val art = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
        if (!art.isNullOrBlank()) {
            AsyncImage(
                model = art,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
