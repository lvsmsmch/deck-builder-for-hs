package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        SectionBlock {
            Text(
                text = stringResource(R.string.deck_stats_mana_curve),
                style = MaterialTheme.typography.labelSmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
            ManaCurve(entries = entries, height = 78.dp, horizontalPadding = 0.dp, verticalPadding = 0.dp)
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

private fun computeStats(entries: List<DeckCardEntry>): DeckStats {
    if (entries.isEmpty()) return DeckStats(0)

    var totalDust = 0

    entries.forEach { entry ->
        val n = entry.count

        entry.card.rarity?.let { r ->
            val craft = r.craftingCost.firstOrNull() ?: 0
            totalDust += craft * n
        }
    }

    return DeckStats(
        totalDust = totalDust,
    )
}

private fun formatDust(value: Int): String =
    value.toString()
