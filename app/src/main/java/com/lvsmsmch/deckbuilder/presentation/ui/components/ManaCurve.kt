package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Mana-curve histogram for [entries]. Buckets 0..6 are exact mana costs;
 * bucket "7+" sums everything ≥ 7.
 */
@Composable
fun ManaCurve(
    entries: List<DeckCardEntry>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 84.dp,
) {
    val buckets = remember(entries) { bucketize(entries) }
    val maxValue = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        buckets.forEachIndexed { index, count ->
            Column(
                modifier = Modifier.width(28.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (count > 0) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
                ManaBar(
                    fraction = count.toFloat() / maxValue.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height - 32.dp),
                )
                Text(
                    text = if (index == 7) "7+" else index.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
    }
}

@Composable
private fun ManaBar(fraction: Float, modifier: Modifier) {
    val safeFraction = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barHeight = (size.height * safeFraction).coerceAtLeast(if (safeFraction > 0f) 2f else 0f)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF5BA6FF), Color(0xFF1F4CC0)),
                    startY = size.height - barHeight,
                    endY = size.height,
                ),
                topLeft = Offset(0f, size.height - barHeight),
                size = Size(size.width, barHeight),
            )
        }
    }
}

private fun bucketize(entries: List<DeckCardEntry>): IntArray {
    val arr = IntArray(8)
    entries.forEach { e ->
        val idx = e.card.manaCost.coerceAtLeast(0).coerceAtMost(7)
        arr[idx] += e.count
    }
    return arr
}

@Composable
fun ManaCurveCard(entries: List<DeckCardEntry>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeckBuilderColors.SurfaceContainer),
    ) {
        ManaCurve(entries = entries)
    }
}
