package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

data class DeckStats(
    val totalDust: Int,
    val legendaryCount: Int,
    val minionCount: Int,
    val spellCount: Int,
    val weaponCount: Int,
    val otherCount: Int,
)

@Composable
fun DeckStatsPanel(deck: Deck, modifier: Modifier = Modifier) {
    DeckStatsPanel(entries = deck.cards, modifier = modifier)
}

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatsText("×${stats.legendaryCount} legendary")
            StatsText("×${stats.minionCount} minions")
            StatsText("×${stats.spellCount} spells")
            StatsText("×${stats.weaponCount} weapons")
            StatsText("×${stats.otherCount} other")
            StatsText("${stats.totalDust} dust")
        }
    }
}

@Composable
private fun StatsText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = DeckBuilderColors.OnSurface)
}

private fun computeStats(entries: List<DeckCardEntry>): DeckStats {
    if (entries.isEmpty()) return DeckStats(0, 0, 0, 0, 0, 0)

    var totalDust = 0
    var legendary = 0
    var minions = 0
    var spells = 0
    var weapons = 0
    var other = 0

    entries.forEach { entry ->
        val n = entry.count

        entry.card.rarity?.let { r ->
            val craft = r.craftingCost.firstOrNull() ?: 0
            totalDust += craft * n
            if (r.slug.equals("legendary", ignoreCase = true)) legendary += n
        }

        when (entry.card.cardType.slug.lowercase()) {
            "minion" -> minions += n
            "spell" -> spells += n
            "weapon" -> weapons += n
            else -> other += n
        }
    }

    return DeckStats(
        totalDust = totalDust,
        legendaryCount = legendary,
        minionCount = minions,
        spellCount = spells,
        weaponCount = weapons,
        otherCount = other,
    )
}
