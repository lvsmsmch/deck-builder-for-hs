package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

data class DeckStats(
    val avgMana: Double,
    val totalDust: Int,
    val rarityCounts: Map<String, Int>,
    val typeCounts: Map<String, Int>,
)

@Composable
fun DeckStatsPanel(deck: Deck, modifier: Modifier = Modifier) {
    val stats = remember(deck) { computeStats(deck) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "STATS",
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            StatBlock(label = "Avg mana", value = "%.1f".format(stats.avgMana), modifier = Modifier.weight(1f))
            StatBlock(label = "Dust", value = formatDust(stats.totalDust), modifier = Modifier.weight(1f))
            StatBlock(
                label = "Spells",
                value = (stats.typeCounts["spell"] ?: 0).toString(),
                modifier = Modifier.weight(1f),
            )
        }

        if (stats.rarityCounts.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            RarityDistribution(stats.rarityCounts)
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .padding(10.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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
            text = "RARITY",
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

private fun computeStats(deck: Deck): DeckStats {
    if (deck.cards.isEmpty()) return DeckStats(0.0, 0, emptyMap(), emptyMap())

    var totalMana = 0
    var totalCount = 0
    var totalDust = 0
    val rarityCounts = mutableMapOf<String, Int>()
    val typeCounts = mutableMapOf<String, Int>()

    deck.cards.forEach { entry ->
        val n = entry.count
        totalCount += n
        totalMana += entry.card.manaCost * n

        entry.card.rarity?.let { r ->
            rarityCounts.merge(r.slug, n, Int::plus)
            // craftingCost: [normal, golden] — use index 0 for normal-quality dust.
            val craft = r.craftingCost.firstOrNull() ?: 0
            totalDust += craft * n
        }
        entry.card.cardType.slug.takeIf { it.isNotBlank() }?.let { t ->
            typeCounts.merge(t, n, Int::plus)
        }
    }

    val avg = if (totalCount > 0) totalMana.toDouble() / totalCount else 0.0
    return DeckStats(
        avgMana = avg,
        totalDust = totalDust,
        rarityCounts = rarityCounts,
        typeCounts = typeCounts,
    )
}

private fun formatDust(value: Int): String =
    if (value >= 1000) "${value / 1000}.${(value % 1000) / 100}k" else value.toString()
