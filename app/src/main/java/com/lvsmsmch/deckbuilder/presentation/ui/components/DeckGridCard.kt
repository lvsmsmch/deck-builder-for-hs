package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.Card

private const val CardAspect = 2f / 3f

@Composable
fun DeckGridCard(
    card: Card,
    count: Int,
    showCount: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    dimImage: Boolean = false,
) {
    Box(
        modifier = modifier
            .aspectRatio(CardAspect),
    ) {
        CardThumbnail(
            card = card,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.fillMaxSize(),
        )
        if (dimImage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.5f)),
            )
        }
        if (showCount && count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD111218)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×$count",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}
