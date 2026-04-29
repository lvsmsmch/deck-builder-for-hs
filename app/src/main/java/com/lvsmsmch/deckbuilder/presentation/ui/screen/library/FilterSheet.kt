package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForRaritySlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.raceLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.rarityLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.spellSchoolLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.typeLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    initial: CardFilters,
    onDismiss: () -> Unit,
    onApply: (CardFilters) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(initial) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeckBuilderColors.SurfaceContainer,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DeckBuilderColors.Outline),
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Header(
                hasFilters = draft.hasFilters,
                onReset = { draft = CardFilters() },
            )

            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 20.dp),
            ) {
                item { ManaSection(draft) { draft = it } }
                item { RaritySection(draft) { draft = it } }
                item { TypeSection(draft) { draft = it } }
                item { MinionTypeSection(draft) { draft = it } }
                item { SpellSchoolSection(draft) { draft = it } }
                item { CollectibleSection(draft) { draft = it } }
                item { Spacer(Modifier.height(12.dp)) }
            }

            Footer(
                onCancel = onDismiss,
                onApply = {
                    onApply(draft)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun Header(hasFilters: Boolean, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.filters_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.action_reset_all),
            style = MaterialTheme.typography.labelMedium,
            color = if (hasFilters) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDimmer,
            modifier = Modifier.clickable(enabled = hasFilters, onClick = onReset),
        )
    }
}

@Composable
private fun Footer(onCancel: () -> Unit, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.action_cancel)) }
        Button(
            onClick = onApply,
            colors = ButtonDefaults.buttonColors(
                containerColor = DeckBuilderColors.Primary,
                contentColor = DeckBuilderColors.OnPrimary,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
        ) { Text(stringResource(R.string.action_apply)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = DeckBuilderColors.OnSurfaceDim,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
    )
}

@Composable
private fun Chip(
    label: String,
    active: Boolean,
    leading: Color? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(
                if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainerHigh,
            )
            .border(
                width = 1.dp,
                color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                shape = RoundedCornerShape(99.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(leading),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun ManaSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_mana))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (0..7).forEach { cost ->
            val active = cost in draft.manaCosts
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainerHigh)
                    .border(
                        1.dp,
                        if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                        RoundedCornerShape(8.dp),
                    )
                    .clickable {
                        val next = if (cost in draft.manaCosts) draft.manaCosts - cost else draft.manaCosts + cost
                        onChange(draft.copy(manaCosts = next))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (cost == 7) "7+" else cost.toString(),
                    color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private val RaritySlugs = listOf("common", "rare", "epic", "legendary")

@Composable
private fun RaritySection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_rarity))
    ChipFlow {
        RaritySlugs.forEach { slug ->
            Chip(
                label = rarityLabel(slug),
                active = slug in draft.rarities,
                leading = colorForRaritySlug(slug),
                onClick = {
                    val next = if (slug in draft.rarities) draft.rarities - slug else draft.rarities + slug
                    onChange(draft.copy(rarities = next))
                },
            )
        }
    }
}

private val TypeSlugs = listOf("minion", "spell", "weapon", "hero", "location")

@Composable
private fun TypeSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_type))
    ChipFlow {
        TypeSlugs.forEach { slug ->
            Chip(
                label = typeLabel(slug),
                active = slug in draft.types,
                onClick = {
                    val next = if (slug in draft.types) draft.types - slug else draft.types + slug
                    onChange(draft.copy(types = next))
                },
            )
        }
    }
}

private val MinionTypeSlugs = listOf(
    "beast", "demon", "dragon", "elemental", "mech",
    "murloc", "naga", "pirate", "quilboar", "totem", "undead",
)

@Composable
private fun MinionTypeSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_minion_type))
    ChipFlow {
        MinionTypeSlugs.forEach { slug ->
            Chip(
                label = raceLabel(slug),
                active = slug in draft.minionTypes,
                onClick = {
                    val next = if (slug in draft.minionTypes) draft.minionTypes - slug else draft.minionTypes + slug
                    onChange(draft.copy(minionTypes = next))
                },
            )
        }
    }
}

private val SpellSchoolSlugs = listOf("arcane", "fire", "frost", "holy", "nature", "shadow", "fel")

@Composable
private fun SpellSchoolSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_spell_school))
    ChipFlow {
        SpellSchoolSlugs.forEach { slug ->
            Chip(
                label = spellSchoolLabel(slug),
                active = slug in draft.spellSchools,
                onClick = {
                    val next = if (slug in draft.spellSchools) draft.spellSchools - slug else draft.spellSchools + slug
                    onChange(draft.copy(spellSchools = next))
                },
            )
        }
    }
}

@Composable
private fun CollectibleSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(R.string.filters_section_show_noncollectible))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Chip(
            label = stringResource(
                if (draft.collectibleOnly) R.string.filters_collectible_only else R.string.filters_all_cards,
            ),
            active = !draft.collectibleOnly,
            onClick = { onChange(draft.copy(collectibleOnly = !draft.collectibleOnly)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}
