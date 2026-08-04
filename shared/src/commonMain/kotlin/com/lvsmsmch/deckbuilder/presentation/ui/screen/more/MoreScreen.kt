package com.lvsmsmch.deckbuilder.presentation.ui.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.presentation.ui.components.Hairline
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenHeader
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoreScreen(
    onOpenSettings: () -> Unit,
    onOpenCardLibrary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        ScreenHeader(title = stringResource(Res.string.more_title))
        Hairline()
        HubRow(
            icon = Icons.Outlined.GridView,
            title = stringResource(Res.string.more_card_library),
            subtitle = stringResource(Res.string.more_card_library_subtitle),
            onClick = onOpenCardLibrary,
        )
        HubRow(
            icon = Icons.Outlined.Settings,
            title = stringResource(Res.string.more_settings),
            subtitle = stringResource(Res.string.more_settings_subtitle),
            onClick = onOpenSettings,
        )
        Hairline()
    }
}

@Composable
private fun HubRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.size(20.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = title, style = AppType.rowName, color = DeckBuilderColors.OnSurface)
                Text(
                    text = subtitle,
                    style = AppType.rowSub,
                    color = DeckBuilderColors.OnSurfaceDimmer,
                )
            }
            Text(text = "›", style = AppType.deckName, color = DeckBuilderColors.OnSurfaceDimmer)
        }
        Hairline(color = DeckBuilderColors.OutlineSoft)
    }
}
