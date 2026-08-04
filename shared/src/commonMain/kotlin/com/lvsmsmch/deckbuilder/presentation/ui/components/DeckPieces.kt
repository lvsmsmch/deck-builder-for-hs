package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
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

/**
 * Mana cost. Blue lives here and in the curve, nowhere else, so a glance at a
 * screen tells you which numbers are costs. [size] is the crystal height.
 */
@Composable
fun ManaGem(
    cost: Int,
    modifier: Modifier = Modifier,
    size: Dp = 29.dp,
) {
    val mana = DeckBuilderColors.Mana
    Box(
        modifier = modifier
            .width(size * (26f / 29f))
            .height(size)
            .clip(GemShape)
            .background(
                Brush.linearGradient(
                    listOf(mana, mana.copy(alpha = 1f).darken(0.45f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cost.toString(),
            color = if (DeckBuilderColors.IsDark) Color(0xFF04101B) else Color.White,
            textAlign = TextAlign.Center,
            style = AppType.gem.copy(fontSize = AppType.gem.fontSize * (size / 29.dp)),
        )
    }
}

private fun Color.darken(amount: Float): Color =
    Color(red * (1f - amount), green * (1f - amount), blue * (1f - amount), alpha)

/** Small rarity indicator on the trailing edge of a row. */
@Composable
fun RarityDot(slug: String?, modifier: Modifier = Modifier, size: Dp = 7.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorForRaritySlug(slug)),
    )
}

/** One-pixel rule along the top edge — the app's only structural device. */
@Composable
fun Modifier.hairlineTop(color: Color = DeckBuilderColors.OutlineSoft): Modifier {
    return this.drawBehind {
        drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1f)
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = DeckBuilderColors.Outline) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(color))
}

/** Tracked capitals: section headers and stat captions. */
@Composable
fun MicroLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = DeckBuilderColors.OnSurfaceDimmer,
) {
    Text(text = text.uppercase(), style = AppType.micro, color = color, modifier = modifier)
}

/**
 * The core list row. Art enters as a shard, the crystal sits over it, and the
 * trailing slot carries whatever the screen needs — a copy count, an add
 * affordance, nothing at all. 54dp, hairline-separated, never a floating card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardListRow(
    manaCost: Int,
    name: String,
    raritySlug: String?,
    artUrl: String?,
    gradient: Pair<Color, Color>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    dimmed: Boolean = false,
    showRarityDot: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val alpha = if (dimmed) 0.4f else 1f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(DeckBuilderColors.Surface)
            .hairlineTop()
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                } else {
                    Modifier
                }
            ),
    ) {
        ArtShard(
            artUrl = artUrl,
            gradient = gradient,
            modifier = Modifier.width(152.dp).fillMaxHeight(),
            alpha = alpha * 0.85f,
            // The subtitle runs across this art, so it steps back further than
            // the deck row's, where the name is large enough to hold its own.
            fadeFrom = 0.08f,
            veil = 0.58f,
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ManaGem(cost = manaCost, size = 29.dp, modifier = Modifier.alpha(alpha))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = AppType.rowName,
                    color = DeckBuilderColors.OnSurface.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppType.rowSub,
                        color = DeckBuilderColors.OnSurfaceDimmer.copy(alpha = alpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (showRarityDot) RarityDot(raritySlug, modifier = Modifier.alpha(alpha))
            trailing?.invoke()
        }
    }
}

/** Copy counter at the end of a deck row. */
@Composable
fun CopyCount(count: Int, dimmed: Boolean = false) {
    Text(
        text = "×$count",
        style = AppType.mono,
        color = DeckBuilderColors.OnSurfaceDim.copy(alpha = if (dimmed) 0.4f else 1f),
    )
}

/** Add affordance at the end of a pool row — brass, because it acts. */
@Composable
fun AddChip() {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, DeckBuilderColors.Secondary, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = AppType.button.copy(fontSize = 17.sp, letterSpacing = 0.sp),
            color = DeckBuilderColors.Primary,
        )
    }
}

/**
 * The deck's shape at full size: a bar per mana cost with its count above it,
 * a hairline baseline, and a dashed brass marker at the deck's average — the
 * one figure a list of cards can never show you.
 */
@Composable
fun ManaCurve(
    counts: List<Int>,
    modifier: Modifier = Modifier,
    average: Double? = null,
    barHeight: Dp = 74.dp,
    showAxis: Boolean = true,
) {
    val max = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    val mana = DeckBuilderColors.Mana
    val marker = DeckBuilderColors.Primary
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val width = maxWidth
            Row(
                modifier = Modifier.fillMaxWidth().height(barHeight + 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                counts.forEach { value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (value > 0) {
                            Text(
                                text = value.toString(),
                                style = AppType.monoSmall,
                                color = DeckBuilderColors.OnSurfaceDim,
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    if (value == 0) 2.dp else barHeight * (value.toFloat() / max),
                                )
                                .background(
                                    if (value == 0) {
                                        Brush.verticalGradient(
                                            listOf(
                                                DeckBuilderColors.OutlineSoft,
                                                DeckBuilderColors.OutlineSoft,
                                            ),
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(mana, mana.copy(alpha = 0.4f)),
                                        )
                                    },
                                ),
                        )
                    }
                }
            }
            if (average != null && average > 0.0) {
                val fraction = (average / counts.size.toDouble()).coerceIn(0.0, 1.0).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val x = size.width * fraction
                            drawLine(
                                color = marker,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f,
                                cap = StrokeCap.Butt,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                            )
                        },
                )
            }
        }
        if (showAxis) {
            Spacer(Modifier.height(6.dp))
            Hairline()
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                counts.indices.forEach { index ->
                    Text(
                        text = if (index == counts.lastIndex) "7+" else index.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = AppType.monoSmall,
                        color = DeckBuilderColors.OnSurfaceDimmer,
                    )
                }
            }
        }
    }
}

private fun formatAverage(value: Double): String {
    val rounded = (value * 10).toInt()
    return "${rounded / 10}.${rounded % 10}"
}

/**
 * The same shape at list size. Fixed-width bars rather than weights, so the
 * silhouette stays comparable between decks; the peak bucket takes full blue.
 */
@Composable
fun CurveSpark(
    counts: List<Int>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 5.dp,
    height: Dp = 22.dp,
) {
    val max = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        counts.forEach { value ->
            val isPeak = value == max && value > 0
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(if (value == 0) 1.dp else height * (value.toFloat() / max))
                    .background(
                        when {
                            value == 0 -> DeckBuilderColors.OutlineSoft
                            isPeak -> DeckBuilderColors.Mana
                            else -> DeckBuilderColors.ManaDim
                        },
                    ),
            )
        }
    }
}

/** Hairline meter: how full a deck is. Brass, because it tracks your action. */
@Composable
fun DeckProgress(cardCount: Int, maxCardCount: Int, modifier: Modifier = Modifier) {
    val fraction = if (maxCardCount > 0) (cardCount.toFloat() / maxCardCount).coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(DeckBuilderColors.Outline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(DeckBuilderColors.Primary),
        )
    }
}

/** One figure with its caption, as used in the deck stat band. */
@Composable
fun StatValue(value: String, caption: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = value, style = AppType.figure, color = DeckBuilderColors.OnSurface)
        MicroLabel(caption)
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
