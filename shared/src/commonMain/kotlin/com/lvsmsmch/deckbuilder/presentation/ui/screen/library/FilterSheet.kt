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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.CardClassScope
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForRaritySlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classShortLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.expansionLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatFilterLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.raceLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.rarityLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.spellSchoolLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.typeLabel
import androidx.compose.ui.text.font.FontWeight
import com.lvsmsmch.deckbuilder.presentation.ui.components.ActionBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.PrimaryButton
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Live-update filter sheet: every chip toggle is committed to [onChange]
 * immediately, the underlying screen re-runs its query, and the user closes
 * the sheet whenever they're satisfied. No Apply/Cancel buttons — that pattern
 * forced an extra confirm step for what's effectively a live-tunable view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    current: CardFilters,
    onChange: (CardFilters) -> Unit,
    onDismiss: () -> Unit,
    resultCount: Int? = null,
    classScopeLabel: String? = null,
    showFormatSection: Boolean = true,
    showClassSection: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sheet sits on Surface so the SurfaceContainer chips read as raised
    // (lighter) in BOTH themes; with a SurfaceContainer sheet the light theme
    // inverted (white sheet, darker chips).
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = DeckBuilderColors.OnSurface,
        scrimColor = if (DeckBuilderColors.IsDark) Color(0x8C05070B) else Color(0x59161B23),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) {
      // The sheet paints its own glass on the content column, whose height is
      // always its own — a backing measured from the outside stopped short of a
      // lazy list that settles late.
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
              .background(DeckBuilderColors.Surface.copy(alpha = 0.94f))
              .background(DeckBuilderColors.SurfaceContainerHigh),
      ) {
          Box(
              modifier = Modifier
                  .padding(top = 12.dp, bottom = 2.dp)
                  .align(Alignment.CenterHorizontally)
                  .size(width = 38.dp, height = 4.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(DeckBuilderColors.Outline),
          )
        Column(modifier = Modifier.fillMaxWidth()) {
            Header(
                hasFilters = current.hasFilters,
                onReset = { onChange(CardFilters()) },
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 520.dp)
                    .padding(horizontal = 20.dp),
            ) {
                item { ManaSection(current, onChange) }
                classScopeLabel?.let { label ->
                    item { ClassScopeSection(label, current, onChange) }
                }
                if (showClassSection) {
                    item { ClassSection(current, onChange) }
                }
                if (showFormatSection) {
                    item { FormatSection(current, onChange) }
                }
                item { RaritySection(current, onChange) }
                item { TypeSection(current, onChange) }
                item { MinionTypeSection(current, onChange) }
                item { SpellSchoolSection(current, onChange) }
                item { SetSection(current, onChange) }
                item { CollectibleSection(current, onChange) }
                item { Spacer(Modifier.height(12.dp)) }
            }

            if (resultCount != null) {
                ActionBar(applyNavigationInset = false) {
                    PrimaryButton(
                        text = stringResource(Res.string.filters_show_results, resultCount),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Spacer(Modifier.height(18.dp))
            }
        }
      }
    }
}

@Composable
private fun ClassSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(Res.string.filters_section_class), draft.classes.size)
    ChipFlow {
        CardLabels.ClassOrder.forEach { slug ->
            Chip(
                label = classShortLabel(slug),
                active = slug in draft.classes,
                leading = colorForClassSlug(slug),
                onClick = {
                    val next = if (slug in draft.classes) draft.classes - slug else draft.classes + slug
                    onChange(draft.copy(classes = next))
                },
            )
        }
    }
}

@Composable
private fun ClassScopeSection(
    classLabel: String,
    draft: CardFilters,
    onChange: (CardFilters) -> Unit,
) {
    SectionHeader(stringResource(Res.string.filters_section_card_pool))
    ChipFlow {
        Chip(
            label = stringResource(Res.string.filters_pool_all),
            active = draft.classScope == CardClassScope.ALL,
            onClick = { onChange(draft.copy(classScope = CardClassScope.ALL)) },
        )
        Chip(
            label = stringResource(Res.string.filters_pool_class_only, classLabel),
            active = draft.classScope == CardClassScope.CLASS_ONLY,
            onClick = { onChange(draft.copy(classScope = CardClassScope.CLASS_ONLY)) },
        )
        Chip(
            label = stringResource(Res.string.filters_pool_neutral_only),
            active = draft.classScope == CardClassScope.NEUTRAL_ONLY,
            onClick = { onChange(draft.copy(classScope = CardClassScope.NEUTRAL_ONLY)) },
        )
    }
}

@Composable
private fun FormatSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(Res.string.filters_section_format))
    ChipFlow {
        listOf(CardFormatFilter.STANDARD, CardFormatFilter.WILD).forEach { format ->
            Chip(
                label = formatFilterLabel(format),
                active = draft.format == format,
                onClick = {
                    val next = if (draft.format == format) {
                        CardFormatFilter.ALL
                    } else {
                        format
                    }
                    onChange(draft.copy(format = next))
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
            text = stringResource(Res.string.filters_title).uppercase(),
            style = AppType.screenTitle.copy(fontSize = 22.sp),
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = hasFilters, onClick = onReset)
                .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_reset_all).uppercase(),
                style = AppType.micro,
                color = if (hasFilters) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDimmer,
            )
        }
    }
}

/** Section label with the count of choices active inside it. */
@Composable
private fun SectionHeader(title: String, selected: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = AppType.micro,
            color = DeckBuilderColors.OnSurfaceDimmer,
        )
        if (selected > 0) {
            Spacer(Modifier.width(7.dp))
            Text(
                text = selected.toString(),
                style = AppType.monoSmall,
                color = DeckBuilderColors.Primary,
            )
        }
    }
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
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (active) DeckBuilderColors.PrimarySoft else androidx.compose.ui.graphics.Color.Transparent,
            )
            .border(
                width = 1.dp,
                color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.Outline,
                shape = RoundedCornerShape(2.dp),
            )
            .clickable(onClick = onClick)
            .height(38.dp)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(leading),
            )
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (active) DeckBuilderColors.OnSurface else DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun ManaSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(Res.string.filters_section_mana), draft.manaCosts.size)
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
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) {
                            DeckBuilderColors.Mana.copy(alpha = 0.14f)
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                    )
                    .border(
                        1.dp,
                        if (active) DeckBuilderColors.Mana else DeckBuilderColors.Outline,
                        RoundedCornerShape(2.dp),
                    )
                    .clickable {
                        val next = if (cost in draft.manaCosts) draft.manaCosts - cost else draft.manaCosts + cost
                        onChange(draft.copy(manaCosts = next))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (cost == 7) "7+" else cost.toString(),
                    color = if (active) DeckBuilderColors.Mana else DeckBuilderColors.OnSurfaceDim,
                    style = AppType.mono,
                )
            }
        }
    }
}

private val RaritySlugs = listOf("common", "rare", "epic", "legendary")

private val SetSlugs = listOf(
    "core",
    "naxx",
    "gvg",
    "brm",
    "tgt",
    "loe",
    "og",
    "kara",
    "gangs",
    "ungoro",
    "icecrown",
    "lootapalooza",
    "gilneas",
    "boomsday",
    "troll",
    "dalaran",
    "uldum",
    "dragons",
    "black-temple",
    "scholomance",
    "darkmoon-faire",
    "the-barrens",
    "stormwind",
    "alterac-valley",
    "the-sunken-city",
    "revendreth",
    "return-of-the-lich-king",
    "battle-of-the-bands",
    "titans",
    "wild-west",
    "whizbangs-workshop",
    "island-vacation",
    "space",
    "the-lost-city",
)

@Composable
private fun SetSection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(Res.string.filters_section_set), draft.sets.size)
    ChipFlow {
        SetSlugs.asReversed().forEach { slug ->
            Chip(
                label = expansionLabel(slug, slug.toSetFallbackLabel()),
                active = slug in draft.sets,
                onClick = {
                    val next = if (slug in draft.sets) draft.sets - slug else draft.sets + slug
                    onChange(draft.copy(sets = next))
                },
            )
        }
    }
}

@Composable
private fun RaritySection(draft: CardFilters, onChange: (CardFilters) -> Unit) {
    SectionHeader(stringResource(Res.string.filters_section_rarity), draft.rarities.size)
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
    SectionHeader(stringResource(Res.string.filters_section_type), draft.types.size)
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
    SectionHeader(stringResource(Res.string.filters_section_minion_type), draft.minionTypes.size)
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
    SectionHeader(stringResource(Res.string.filters_section_spell_school), draft.spellSchools.size)
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
    SectionHeader(stringResource(Res.string.filters_section_show_noncollectible))
    Text(
        text = stringResource(Res.string.filters_show_noncollectible_description),
        style = MaterialTheme.typography.bodySmall,
        color = DeckBuilderColors.OnSurface,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Chip(
            label = stringResource(Res.string.filters_show_noncollectible_on),
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

private fun String.toSetFallbackLabel(): String =
    split('-', '_').joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercaseChar() }
    }
