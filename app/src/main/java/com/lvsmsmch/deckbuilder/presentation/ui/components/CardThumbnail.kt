package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        modifier = modifier
            .aspectRatio(0.65f)
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
        // Card art (cropImage is the cleaner crop; falls back to full render).
        val art = card.cropImage?.takeIf { it.isNotBlank() } ?: card.image
        if (art.isNotBlank()) {
            AsyncImage(
                model = art,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Mana gem
        ManaGem(
            cost = card.manaCost,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp),
        )

        // Bottom gradient + name + stats
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color(0xCC000000),
                        1f to Color(0xEE000000),
                    ),
                ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = card.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 4.dp),
                style = TextStyle(
                    color = Color(0xFFF5F6F8),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    shadow = Shadow(color = Color(0xCC000000), blurRadius = 2f),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xAA000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatBadge(value = card.attack, color = Color(0xFFFFE9B0))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(rarityColor)
                        .padding(3.dp),
                )
                StatBadge(value = card.health ?: card.durability, color = Color(0xFFFFB0B0))
            }
        }
    }
}

@Composable
private fun StatBadge(value: Int?, color: Color) {
    Text(
        text = value?.toString() ?: "",
        style = TextStyle(
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            shadow = Shadow(color = Color(0xCC000000), blurRadius = 2f),
            fontFeatureSettings = "tnum",
        ),
    )
}
