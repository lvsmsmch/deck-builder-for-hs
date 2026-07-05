package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun DeckCardRow(
    entry: DeckCardEntry,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val card = entry.card

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ManaGem(
            cost = card.manaCost,
            size = 30.dp,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        ) {
            CardTile(
                slug = card.slug,
                contentDescription = card.name,
                verticalFocus = 0.36f,
                modifier = Modifier.fillMaxSize(),
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
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp, end = 30.dp),
                maxLines = 1,
            )
            if (card.rarity?.slug.equals("legendary", ignoreCase = true)) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = DeckBuilderColors.Rarity.Legendary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(20.dp),
                )
            }
        }

        CountPill(count = entry.count)
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DeckBuilderColors.OnSurface.copy(alpha = 0.88f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = null,
                    tint = DeckBuilderColors.Surface,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
private fun CountPill(count: Int) {
    Box(
        modifier = Modifier
            .width(38.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "x$count",
            style = MaterialTheme.typography.labelMedium,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}
