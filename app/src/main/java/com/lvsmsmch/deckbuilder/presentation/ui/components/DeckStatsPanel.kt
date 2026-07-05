package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

data class DeckStats(
    val totalDust: Int,
    val rarityCounts: Map<String, Int>,
)

@Composable
fun DeckStatsPanel(deck: Deck, modifier: Modifier = Modifier) {
    DeckStatsPanel(entries = deck.cards, modifier = modifier)
}

@Composable
fun DeckStatsPanel(entries: List<DeckCardEntry>, modifier: Modifier = Modifier) {
    val stats = remember(entries) { computeStats(entries) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(
            text = stringResource(R.string.deck_stats_title),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
        Spacer(Modifier.height(10.dp))
        StatBlock(
            label = stringResource(R.string.deck_stats_dust),
            value = formatDust(stats.totalDust),
            valueStyle = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp),
        )
        Spacer(Modifier.height(10.dp))
        SectionBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp),
        ) {
            Text(
                text = stringResource(R.string.deck_stats_mana_curve),
                style = MaterialTheme.typography.labelSmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
            ManaCurve(entries = entries, height = 86.dp, horizontalPadding = 0.dp, verticalPadding = 0.dp)
        }

        if (stats.rarityCounts.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            SectionBlock {
                RarityDistribution(stats.rarityCounts)
            }
        }
    }
}

@Composable
private fun SectionBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .padding(10.dp),
        content = content,
    )
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = valueStyle,
            color = DeckBuilderColors.OnSurface,
        )
    }
}

@Composable
private fun RarityDistribution(rarityCounts: Map<String, Int>) {
    val total = rarityCounts.values.sum().coerceAtLeast(1)
    val ordered = listOf("common", "rare", "epic", "legendary").mapNotNull { slug ->
        val count = rarityCounts[slug] ?: 0
        if (count == 0) null else slug to count
    }
    if (ordered.isEmpty()) return

    Column {
        Text(
            text = stringResource(R.string.deck_stats_rarity),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            ordered.forEach { (slug, count) ->
                Box(
                    modifier = Modifier
                        .background(colorForRaritySlug(slug))
                        .weight(count.toFloat() / total.toFloat())
                        .fillMaxWidth()
                        .height(8.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ordered.forEach { (slug, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colorForRaritySlug(slug)),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
            }
        }
    }
}

private fun computeStats(entries: List<DeckCardEntry>): DeckStats {
    if (entries.isEmpty()) return DeckStats(0, emptyMap())

    var totalDust = 0
    val rarityCounts = mutableMapOf<String, Int>()

    entries.forEach { entry ->
        val n = entry.count

        entry.card.rarity?.let { r ->
            rarityCounts.merge(r.slug, n, Int::plus)
            val craft = r.craftingCost.firstOrNull() ?: 0
            totalDust += craft * n
        }
    }

    return DeckStats(
        totalDust = totalDust,
        rarityCounts = rarityCounts,
    )
}

private fun formatDust(value: Int): String =
    value.toString()
