package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Single row in a deck listing: mana gem, horizontal art tile, card name,
 * count badge. The tile uses [ContentScale.Crop] (HsJson tiles are 256×59,
 * we render them at the row height while letting the tile crop horizontally
 * rather than stretch — earlier `FillBounds` made art look squished).
 */
@Composable
fun DeckCardRow(
    entry: DeckCardEntry,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val card = entry.card
    val isLegendary = card.rarity?.slug?.equals("legendary", ignoreCase = true) == true

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ManaGem(cost = card.manaCost, size = 24.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        ) {
            CardTile(
                slug = card.slug,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Subtle dark gradient at the left side so the name overlay reads
            // cleanly against any card art.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            0f to androidx.compose.ui.graphics.Color(0xCC000000),
                            0.55f to androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    ),
            )
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleSmall,
                color = DeckBuilderColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp, end = 8.dp),
                maxLines = 1,
            )
        }

        CountPill(count = entry.count, isLegendary = isLegendary)
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
private fun CountPill(count: Int, isLegendary: Boolean) {
    if (isLegendary) {
        Text(
            text = "★",
            style = MaterialTheme.typography.titleMedium,
            color = DeckBuilderColors.Rarity.Legendary,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    } else {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DeckBuilderColors.SurfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "×$count",
                style = MaterialTheme.typography.labelMedium,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
    }
}
