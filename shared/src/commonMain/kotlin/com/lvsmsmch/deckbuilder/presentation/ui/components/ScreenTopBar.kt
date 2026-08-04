package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.action_back
import org.jetbrains.compose.resources.stringResource

/**
 * Back arrow + title, optionally followed by trailing actions. One definition
 * so every screen keeps the same paddings and title size.
 */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    bottomPadding: Dp = 8.dp,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = bottomPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = DeckBuilderColors.OnSurface,
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text = title,
            style = titleStyle,
            color = DeckBuilderColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}
