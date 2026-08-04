package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Screen header: the title in condensed capitals, a monospaced line of counts
 * under it, and at most one control on the right.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(bottom = 2.dp)) { leading() }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                style = AppType.screenTitle,
                color = DeckBuilderColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = AppType.monoSmall,
                    color = DeckBuilderColors.OnSurfaceDimmer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing()
    }
}

/** Bordered, tracked capitals. Quiet by design — it is not the screen's action. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = AppType.microSmall,
            color = DeckBuilderColors.OnSurfaceDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 132.dp),
        )
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** Square bordered icon control; [active] turns it brass. */
@Composable
fun BorderedIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .border(
                1.dp,
                if (active) DeckBuilderColors.Primary else DeckBuilderColors.Outline,
                RoundedCornerShape(2.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** The screen's one action. Brass is reserved for exactly this. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val alpha = if (enabled) 1f else 0.4f
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DeckBuilderColors.Primary.copy(alpha = alpha))
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = DeckBuilderColors.OnPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text.uppercase(),
                style = AppType.button,
                color = DeckBuilderColors.OnPrimary,
                maxLines = 1,
            )
        }
    }
}

/** The second action, when there is one. Outline only — never a second fill. */
@Composable
fun QuietButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(2.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = AppType.button,
            color = DeckBuilderColors.OnSurfaceDim.copy(alpha = if (enabled) 1f else 0.4f),
            maxLines = 1,
        )
    }
}

/** Bottom bar that carries the screen's actions, on the receding container. */
@Composable
fun ActionBar(
    modifier: Modifier = Modifier,
    /** False inside the tab host, where the bottom bar already ate the inset. */
    applyNavigationInset: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(DeckBuilderColors.SurfaceContainer)) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (applyNavigationInset) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/**
 * The card. Everything the eye should treat as one object gets one of these:
 * a row, a panel, a settings group. Pale plate, hairline edge, corners all but
 * square — laid on the ground rather than carved out of it.
 */
@Composable
fun Plate(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .border(1.dp, DeckBuilderColors.Outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()
    }
}

/** Small tracked label with an optional accent, used for formats and states. */
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DeckBuilderColors.OnSurfaceDim,
    borderColor: Color = DeckBuilderColors.Outline,
) {
    Text(
        text = text.uppercase(),
        style = AppType.microSmall,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

/** State pill with a leading dot — reads at a glance before the words do. */
@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(2.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(text = text.uppercase(), style = AppType.microSmall, color = color, maxLines = 1)
    }
}

/** Section rule: tracked capitals on the left, an optional counterpart right. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MicroLabel(text)
        if (trailing != null) MicroLabel(trailing)
    }
}

/** Warning rail: hairline box with a thick brass edge on the leading side. */
@Composable
fun NoticeRow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DeckBuilderColors.Primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.5f))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(modifier = Modifier.width(3.dp).height(16.dp).background(color))
        Text(
            text = text,
            style = AppType.rowSub.copy(fontSize = AppType.rowName.fontSize * 0.86f),
            color = DeckBuilderColors.OnSurface,
        )
    }
}
