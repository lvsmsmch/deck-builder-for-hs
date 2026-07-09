package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.koinInject

data class DeckStats(
    val cardCount: Int,
    val avgManaCost: Double,
    val totalDust: Int,
    val legendaryCount: Int,
    val minionCount: Int,
    val spellCount: Int,
    val weaponCount: Int,
    val otherCount: Int,
)

/** Deck composition dialog opened from the deck "⋮ → Info" menus. */
@Composable
fun DeckStatsDialog(deck: Deck, onDismiss: () -> Unit) {
    val stats = remember(deck) { computeStats(deck.cards) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(R.string.deck_stats_title), color = DeckBuilderColors.OnSurface) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StatRow(stringResource(R.string.stats_cards), "${deck.cardCount}/${deck.maxCardCount}")
                StatRow(stringResource(R.string.stats_avg_mana), "%.1f".format(stats.avgManaCost))
                StatRow(stringResource(R.string.stats_minions), stats.minionCount.toString())
                StatRow(stringResource(R.string.stats_spells), stats.spellCount.toString())
                StatRow(stringResource(R.string.stats_weapons), stats.weaponCount.toString())
                StatRow(stringResource(R.string.stats_other), stats.otherCount.toString())
                StatRow(stringResource(R.string.stats_legendary), stats.legendaryCount.toString())
                StatRow(stringResource(R.string.stats_dust), stats.totalDust.toString())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = DeckBuilderColors.OnSurface)
            }
        },
    )
}

/**
 * Variant for callers that only have the deck code (saved-decks list). The
 * deck is usually already in the repository memory cache; otherwise a fast
 * targeted decode fills it in.
 */
@Composable
fun DeckStatsDialogForCode(code: String, onDismiss: () -> Unit) {
    val decks: DeckRepository = koinInject()
    var deck by remember(code) { mutableStateOf(decks.cachedDeck(code)) }

    LaunchedEffect(code) {
        if (deck == null) {
            when (val r = decks.decodeByCode(code)) {
                is Result.Success -> deck = r.data
                is Result.Error -> onDismiss()
            }
        }
    }

    deck?.let { DeckStatsDialog(deck = it, onDismiss = onDismiss) }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurface,
        )
    }
}

private fun computeStats(entries: List<DeckCardEntry>): DeckStats {
    if (entries.isEmpty()) return DeckStats(0, 0.0, 0, 0, 0, 0, 0, 0)

    var count = 0
    var manaSum = 0L
    var totalDust = 0
    var legendary = 0
    var minions = 0
    var spells = 0
    var weapons = 0
    var other = 0

    entries.forEach { entry ->
        val n = entry.count
        count += n
        manaSum += entry.card.manaCost.toLong() * n

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
        cardCount = count,
        avgManaCost = if (count > 0) manaSum.toDouble() / count else 0.0,
        totalDust = totalDust,
        legendaryCount = legendary,
        minionCount = minions,
        spellCount = spells,
        weaponCount = weapons,
        otherCount = other,
    )
}
