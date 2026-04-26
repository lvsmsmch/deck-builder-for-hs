package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun DeckCardRow(
    entry: DeckCardEntry,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val card = entry.card
    val rarityColor = rarityColor(card.rarity)
    val isLegendary = card.rarity?.slug?.equals("legendary", ignoreCase = true) == true

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ManaGem(cost = card.manaCost, size = 26.dp)

        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(rarityColor),
        )

        Text(
            text = card.name,
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

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
