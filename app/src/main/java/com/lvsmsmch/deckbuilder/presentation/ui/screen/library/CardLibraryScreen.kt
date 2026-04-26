package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardLibraryScreen(
    initialKeyword: String? = null,
    initialSetSlug: String? = null,
    onCardClick: (Card) -> Unit = {},
    viewModel: CardLibraryViewModel = koinViewModel(
        parameters = { org.koin.core.parameter.parametersOf(initialKeyword, initialSetSlug) },
    ),
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val nearEnd by remember {
        derivedStateOf {
            val total = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 10
        }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { nearEnd }.distinctUntilChanged().collect { atEnd ->
            if (atEnd) viewModel.loadNextPage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        Header(
            totalCount = state.totalCount,
            sort = state.filters.sort,
            onSortChange = { viewModel.setSort(it.key, it.direction) },
            onOpenFilters = { showFilterSheet = true },
            activeFilterCount = state.filters.activeFilterCount(),
        )

        state.newSetBanner?.let { newSet ->
            NewSetBanner(
                set = newSet,
                onOpen = viewModel::openNewSetBanner,
                onDismiss = viewModel::dismissNewSetBanner,
            )
        }

        SearchField(
            query = state.filters.textQuery,
            onQueryChange = viewModel::setTextQuery,
        )

        ManaChips(
            selected = state.filters.manaCosts,
            onToggle = viewModel::toggleManaCost,
        )

        ClassChips(
            selected = state.filters.classes,
            metadata = state.metadata,
            onToggle = viewModel::toggleClass,
        )

        when {
            state.isLoadingFirstPage && state.cards.isEmpty() -> CenteredSpinner()
            state.cards.isEmpty() && state.errorMessage == null -> EmptyState(
                hasFilters = state.filters.hasFilters,
            )
            state.errorMessage != null && state.cards.isEmpty() -> ErrorState(
                message = state.errorMessage!!,
                onRetry = viewModel::retry,
            )
            else -> CardGrid(
                state = state,
                gridState = gridState,
                onCardClick = onCardClick,
            )
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            initial = state.filters,
            metadata = state.metadata,
            onDismiss = { showFilterSheet = false },
            onApply = viewModel::applyFilters,
        )
    }
}

private fun com.lvsmsmch.deckbuilder.domain.entities.CardFilters.activeFilterCount(): Int {
    var n = 0
    if (classes.isNotEmpty()) n++
    if (sets.isNotEmpty()) n++
    if (rarities.isNotEmpty()) n++
    if (types.isNotEmpty()) n++
    if (minionTypes.isNotEmpty()) n++
    if (keywords.isNotEmpty()) n++
    if (spellSchools.isNotEmpty()) n++
    if (manaCosts.isNotEmpty()) n++
    if (!collectibleOnly) n++
    if (textQuery.isNotBlank()) n++
    return n
}

@Composable
private fun Header(
    totalCount: Int,
    sort: CardSort,
    onSortChange: (CardSort) -> Unit,
    onOpenFilters: () -> Unit,
    activeFilterCount: Int,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )

        // Sort pill — opens dropdown.
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeckBuilderColors.SurfaceContainer)
                    .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(8.dp))
                    .clickable { sortMenuOpen = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sort.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = DeckBuilderColors.OnSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (sort.direction == SortDir.ASC) "↑" else "↓",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
            DropdownMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { sortMenuOpen = false },
            ) {
                SortOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.label) },
                        onClick = {
                            onSortChange(opt.sort)
                            sortMenuOpen = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        Box {
            IconButton(onClick = onOpenFilters) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filters",
                    tint = DeckBuilderColors.OnSurface,
                )
            }
            if (activeFilterCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(DeckBuilderColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = activeFilterCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DeckBuilderColors.OnPrimary,
                        fontSize = 9.sp,
                    )
                }
            }
        }
    }

    if (totalCount > 0) {
        Text(
            text = stringResource(R.string.library_count_format, totalCount),
            style = MaterialTheme.typography.bodySmall,
            color = DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
        )
    }
}

private data class SortOption(val label: String, val sort: CardSort)

private val SortOptions = listOf(
    SortOption("Mana ascending", CardSort(SortKey.MANA_COST, SortDir.ASC)),
    SortOption("Mana descending", CardSort(SortKey.MANA_COST, SortDir.DESC)),
    SortOption("Name", CardSort(SortKey.NAME, SortDir.ASC)),
    SortOption("Newest", CardSort(SortKey.DATE_ADDED, SortDir.DESC)),
    SortOption("Group by class", CardSort(SortKey.GROUP_BY_CLASS, SortDir.ASC)),
    SortOption("Attack", CardSort(SortKey.ATTACK, SortDir.DESC)),
    SortOption("Health", CardSort(SortKey.HEALTH, SortDir.DESC)),
)

private fun CardSort.label(): String = when (key) {
    SortKey.MANA_COST -> "Mana"
    SortKey.NAME -> "Name"
    SortKey.DATE_ADDED -> "Newest"
    SortKey.GROUP_BY_CLASS -> "By class"
    SortKey.ATTACK -> "Attack"
    SortKey.HEALTH -> "Health"
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.library_search_hint), color = DeckBuilderColors.OnSurfaceDimmer) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Clear",
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
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedTextColor = DeckBuilderColors.OnSurface,
            unfocusedTextColor = DeckBuilderColors.OnSurface,
            cursorColor = DeckBuilderColors.Primary,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
}

@Composable
private fun ManaChips(selected: Set<Int>, onToggle: (Int) -> Unit) {
    val costs = remember { (0..7).toList() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        costs.forEach { cost ->
            val active = cost in selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainer,
                    )
                    .border(
                        width = 1.dp,
                        color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onToggle(cost) },
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

@Composable
private fun ClassChips(
    selected: Set<String>,
    metadata: com.lvsmsmch.deckbuilder.domain.entities.Metadata?,
    onToggle: (String) -> Unit,
) {
    // Prefer metadata names so chips localise with the user's chosen card locale.
    // Hardcoded list is the cold-start fallback before metadata lands.
    val fallback = listOf(
        "druid" to "Druid",
        "hunter" to "Hunter",
        "mage" to "Mage",
        "paladin" to "Paladin",
        "priest" to "Priest",
        "rogue" to "Rogue",
        "shaman" to "Shaman",
        "warlock" to "Warlock",
        "warrior" to "Warrior",
        "demonhunter" to "Demon H.",
        "deathknight" to "Death K.",
        "neutral" to "Neutral",
    )
    val classes = remember(metadata) {
        val fromApi = metadata?.classes?.values
            ?.sortedBy { it.id }
            ?.map { it.slug to it.name.ifBlank { it.slug.replaceFirstChar { c -> c.uppercase() } } }
            .orEmpty()
        if (fromApi.isEmpty()) fallback else fromApi
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        classes.forEach { (slug, label) ->
            val active = slug in selected
            val color = colorForClassSlug(slug)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainer,
                    )
                    .border(
                        width = 1.dp,
                        color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                        shape = RoundedCornerShape(99.dp),
                    )
                    .clickable { onToggle(slug) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun CardGrid(
    state: CardLibraryState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onCardClick: (Card) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.cards, key = { it.id }) { card ->
            CardThumbnail(card = card, onClick = { onCardClick(card) })
        }
        if (state.isLoadingMore || state.hasMore) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DeckBuilderColors.Primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyState(hasFilters: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(
                if (hasFilters) R.string.library_empty_with_filters else R.string.library_empty_no_filters,
            ),
            color = DeckBuilderColors.OnSurfaceDim,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = DeckBuilderColors.Error,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary)
                .clickable { onRetry() }
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(R.string.action_retry),
                color = DeckBuilderColors.OnPrimary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
