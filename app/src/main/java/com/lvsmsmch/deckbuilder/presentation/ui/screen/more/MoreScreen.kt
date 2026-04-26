package com.lvsmsmch.deckbuilder.presentation.ui.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@Composable
fun MoreScreen(
    onOpenGlossary: () -> Unit,
    onOpenCardBacks: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        Text(
            text = stringResource(R.string.more_title),
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HubRow(
                icon = Icons.Outlined.MenuBook,
                title = stringResource(R.string.more_glossary),
                subtitle = stringResource(R.string.more_glossary_subtitle),
                onClick = onOpenGlossary,
                position = HubPosition.Top,
            )
            HubRow(
                icon = Icons.Outlined.Style,
                title = stringResource(R.string.more_cardbacks),
                subtitle = stringResource(R.string.more_cardbacks_subtitle),
                onClick = onOpenCardBacks,
                position = HubPosition.Middle,
            )
            HubRow(
                icon = Icons.Outlined.Settings,
                title = stringResource(R.string.more_settings),
                subtitle = stringResource(R.string.more_settings_subtitle),
                onClick = onOpenSettings,
                position = HubPosition.Bottom,
            )
        }
    }
}

private enum class HubPosition { Top, Middle, Bottom, Single }

@Composable
private fun HubRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    position: HubPosition,
) {
    val shape = when (position) {
        HubPosition.Single -> RoundedCornerShape(14.dp)
        HubPosition.Top -> RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        HubPosition.Middle -> RoundedCornerShape(0.dp)
        HubPosition.Bottom -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.SurfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = DeckBuilderColors.OnSurface)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurfaceDimmer,
        )
    }
}
