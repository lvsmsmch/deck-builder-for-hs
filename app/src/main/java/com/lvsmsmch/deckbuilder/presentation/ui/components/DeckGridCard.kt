package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

private const val CardAspect = 2f / 3f

@Composable
fun DeckGridCard(
    card: Card,
    count: Int,
    showCount: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onAdd: (() -> Unit)? = null,
    addEnabled: Boolean = true,
    onRemove: (() -> Unit)? = null,
) {
    val isLight = DeckBuilderColors.Surface.luminance() > 0.5f
    val controlBackground = if (isLight) Color(0xDD111218) else Color.White.copy(alpha = 0.92f)
    val controlForeground = if (isLight) Color.White else Color.Black

    Box(
        modifier = modifier
            .aspectRatio(CardAspect)
            .alpha(if (dimmed) 0.5f else 1f),
    ) {
        CardThumbnail(
            card = card,
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
        )
        if (showCount && count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD111218))
                    .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "x$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
        }
        onAdd?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(controlBackground)
                    .clickable(enabled = addEnabled, onClick = it),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_add),
                    tint = controlForeground,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
        onRemove?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(controlBackground)
                    .clickable(onClick = it),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = stringResource(R.string.action_remove_card),
                    tint = controlForeground,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
