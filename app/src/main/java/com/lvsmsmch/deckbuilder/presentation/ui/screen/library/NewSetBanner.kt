package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Expansion
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun NewSetBanner(
    set: Expansion,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        DeckBuilderColors.Primary.copy(alpha = 0.18f),
                        DeckBuilderColors.SurfaceContainer,
                    ),
                ),
            )
            .border(1.dp, DeckBuilderColors.Primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "★",
                color = DeckBuilderColors.OnPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.library_new_set_title, set.name),
                style = MaterialTheme.typography.titleSmall,
                color = DeckBuilderColors.OnSurface,
            )
            Text(
                text = stringResource(R.string.library_new_set_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onDismiss)
                .padding(6.dp),
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(R.string.action_dismiss),
                tint = DeckBuilderColors.OnSurfaceDim,
            )
        }
    }
}
