package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/** Hexagonal mana crystal, the shape used on every real Hearthstone card. */
private val GemShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.26f)
    lineTo(w, h * 0.74f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.74f)
    lineTo(0f, h * 0.26f)
    close()
}

private val GemFill = Brush.linearGradient(
    0f to Color(0xFF8FC7FF),
    0.62f to Color(0xFF1F5FA8),
    1f to Color(0xFF123A6B),
)

/** Mana cost gem. [size] is the gem height; width follows the 21:23 ratio. */
@Composable
fun ManaGem(
    cost: Int,
    modifier: Modifier = Modifier,
    size: Dp = 23.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
) {
    Box(
        modifier = modifier
            .width(size * (21f / 23f))
            .height(size)
            .clip(GemShape)
            .background(GemFill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cost.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                shadow = Shadow(color = Color(0x99000000), blurRadius = 2f),
            ),
        )
    }
}

/** Small rarity indicator; legendary uses the theme's gold. */
@Composable
fun RarityDot(slug: String?, modifier: Modifier = Modifier, size: Dp = 7.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorForRaritySlug(slug)),
    )
}

/**
 * The core list row: art strip fading into the surface, mana gem over it, name,
 * rarity, and a trailing slot for the copy count or an add affordance.
 * 44dp tall — a full deck fits one screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardListRow(
    manaCost: Int,
    name: String,
    raritySlug: String?,
    artUrl: String?,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    dimmed: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val alpha = if (dimmed) 0.45f else 1f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(10.dp))
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .fillMaxHeight(),
        ) {
            CardArtStrip(artUrl = artUrl, accent = accent, alpha = alpha)
            ManaGem(
                cost = manaCost,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 6.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeckBuilderColors.OnSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = DeckBuilderColors.OnSurfaceDimmer.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
        RarityDot(raritySlug, modifier = Modifier.padding(end = 10.dp))
        trailing?.let {
            it()
            Spacer(Modifier.width(10.dp))
        }
    }
}

/** Copy counter shown at the end of a deck row. */
@Composable
fun CopyCount(count: Int, dimmed: Boolean = false) {
    Text(
        text = "×$count",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = DeckBuilderColors.OnSurfaceDim.copy(alpha = if (dimmed) 0.45f else 1f),
    )
}

/** Add affordance shown at the end of a pool row. */
@Composable
fun AddChip() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(DeckBuilderColors.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = DeckBuilderColors.Primary,
        )
    }
}

/**
 * Mana curve. Bars are relative to the tallest column; the last column is the
 * 7+ bucket, matching the filter row.
 */
@Composable
fun ManaCurve(
    counts: List<Int>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 46.dp,
    showLabels: Boolean = true,
) {
    val max = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(barHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            counts.forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                        .background(DeckBuilderColors.SurfaceContainerHigh),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(value.toFloat() / max)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(DeckBuilderColors.Primary),
                    )
                }
            }
        }
        if (showLabels) {
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                counts.indices.forEach { index ->
                    Text(
                        text = if (index == counts.lastIndex) "7+" else index.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = DeckBuilderColors.OnSurfaceDimmer,
                    )
                }
            }
        }
    }
}

/** Compact curve without labels, used inside saved-deck tiles. */
@Composable
fun MiniManaCurve(counts: List<Int>, modifier: Modifier = Modifier) {
    val max = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        counts.forEach { value ->
            // Empty buckets stay empty: a floor height would read as data.
            val fraction = if (value == 0) 0f else (value.toFloat() / max).coerceAtLeast(0.18f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(DeckBuilderColors.Primary.copy(alpha = 0.6f)),
            )
        }
    }
}

/** Thin completion bar: how full a deck is. */
@Composable
fun DeckProgress(cardCount: Int, maxCardCount: Int, modifier: Modifier = Modifier) {
    val fraction = if (maxCardCount > 0) (cardCount.toFloat() / maxCardCount).coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DeckBuilderColors.Outline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (fraction >= 1f) DeckBuilderColors.Primary else DeckBuilderColors.Primary.copy(alpha = 0.7f),
                ),
        )
    }
}

/** One number with its caption, as used in the deck header stat line. */
@Composable
fun StatValue(value: String, caption: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeckBuilderColors.OnSurface,
        )
        Text(
            text = caption.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = DeckBuilderColors.OnSurfaceDimmer,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Counts cards per mana cost, collapsing everything from 7 upwards. */
fun manaCurveOf(entries: List<DeckCardEntry>, buckets: Int = 8): List<Int> {
    val counts = MutableList(buckets) { 0 }
    entries.forEach { entry ->
        val index = entry.card.manaCost.coerceIn(0, buckets - 1)
        counts[index] = counts[index] + entry.count
    }
    return counts
}

/** Average mana cost across all copies in the deck. */
fun averageManaCost(entries: List<DeckCardEntry>): Double {
    val cards = entries.sumOf { it.count }
    if (cards == 0) return 0.0
    val total = entries.sumOf { it.card.manaCost.toLong() * it.count }
    return total.toDouble() / cards
}

/** Total dust needed to craft every copy in the deck. */
fun craftingCostOf(entries: List<DeckCardEntry>): Int =
    entries.sumOf { entry -> (entry.card.rarity?.craftingCost?.firstOrNull() ?: 0) * entry.count }
