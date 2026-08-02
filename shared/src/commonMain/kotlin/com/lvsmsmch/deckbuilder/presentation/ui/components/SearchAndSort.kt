package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.action_clear
import com.lvsmsmch.deckbuilder.resources.filters_title
import com.lvsmsmch.deckbuilder.resources.library_search_hint
import com.lvsmsmch.deckbuilder.resources.sort_group_by_class
import com.lvsmsmch.deckbuilder.resources.sort_mana_asc
import com.lvsmsmch.deckbuilder.resources.sort_mana_desc
import com.lvsmsmch.deckbuilder.resources.sort_name
import com.lvsmsmch.deckbuilder.resources.sort_newest
import com.lvsmsmch.deckbuilder.resources.sort_oldest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Square toolbar button with an optional count badge (filters, actions). */
@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    badge: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(DeckBuilderColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = DeckBuilderColors.OnPrimary,
                    fontSize = 9.sp,
                )
            }
        }
    }
}

data class SortChoice(val labelRes: StringResource, val sort: CardSort)

/**
 * Sort options. The library additionally groups by class; the builder pool is
 * already class-scoped, so that entry would be a no-op there.
 */
object SortChoices {
    val Pool: List<SortChoice> = listOf(
        SortChoice(Res.string.sort_mana_asc, CardSort(SortKey.MANA_COST, SortDir.ASC)),
        SortChoice(Res.string.sort_mana_desc, CardSort(SortKey.MANA_COST, SortDir.DESC)),
        SortChoice(Res.string.sort_name, CardSort(SortKey.NAME, SortDir.ASC)),
        SortChoice(Res.string.sort_newest, CardSort(SortKey.DATE_ADDED, SortDir.ASC)),
        SortChoice(Res.string.sort_oldest, CardSort(SortKey.DATE_ADDED, SortDir.DESC)),
    )

    val Library: List<SortChoice> =
        Pool + SortChoice(Res.string.sort_group_by_class, CardSort(SortKey.GROUP_BY_CLASS, SortDir.ASC))
}

@Composable
fun SortMenuButton(
    sort: CardSort,
    choices: List<SortChoice>,
    onSortChange: (CardSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    val currentLabel = choices.firstOrNull { it.sort == sort }?.labelRes ?: Res.string.sort_mana_asc
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(10.dp))
                .clickable { sortMenuOpen = true }
                .padding(start = 10.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(currentLabel),
                color = DeckBuilderColors.OnSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = sortMenuOpen,
            onDismissRequest = { sortMenuOpen = false },
        ) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(stringResource(choice.labelRes)) },
                    onClick = {
                        onSortChange(choice.sort)
                        sortMenuOpen = false
                    },
                )
            }
        }
    }
}

/** Search field + filter button, shared by the library and the builder pool. */
@Composable
fun CardSearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
            placeholder = {
                Text(
                    stringResource(Res.string.library_search_hint),
                    color = DeckBuilderColors.OnSurfaceDimmer,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = DeckBuilderColors.OnSurface,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(Res.string.action_clear),
                        tint = DeckBuilderColors.OnSurfaceDim,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onQueryChange("") }
                            .padding(4.dp),
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DeckBuilderColors.SurfaceContainer,
                unfocusedContainerColor = DeckBuilderColors.SurfaceContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = DeckBuilderColors.OnSurface,
                unfocusedTextColor = DeckBuilderColors.OnSurface,
                cursorColor = DeckBuilderColors.Primary,
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp)),
        )
        HeaderIconButton(
            onClick = onOpenFilters,
            badge = activeFilterCount.takeIf { it > 0 }?.toString(),
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = stringResource(Res.string.filters_title),
                tint = DeckBuilderColors.OnSurface,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}
