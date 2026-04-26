package com.lvsmsmch.deckbuilder.presentation.ui.screen.battlegrounds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BattlegroundsScreen(
    onCardClick: (Card) -> Unit = {},
    viewModel: BattlegroundsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()

    val nearEnd by remember {
        derivedStateOf {
            val total = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 8
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
        Header(state.totalCount)
        TabBar(active = state.tab, onSelect = viewModel::selectTab)
        SearchField(query = state.textQuery, onChange = viewModel::setQuery)

        if (state.tab == BgTab.Minions) {
            TierChips(selected = state.tiers, onToggle = viewModel::toggleTier)
            MinionTypeChips(selected = state.minionTypes, onToggle = viewModel::toggleMinionType)
        }

        when {
            state.isLoadingFirstPage && state.cards.isEmpty() -> CenteredSpinner()
            state.cards.isEmpty() && state.errorMessage == null -> EmptyState(state.tab)
            state.errorMessage != null && state.cards.isEmpty() -> ErrorState(
                message = state.errorMessage!!,
                onRetry = viewModel::retry,
            )
            else -> CardGrid(state, gridState, onCardClick)
        }
    }
}

@Composable
private fun Header(total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.bg_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        if (total > 0) {
            Text(
                text = "$total",
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
    }
}

@Composable
private fun TabBar(active: BgTab, onSelect: (BgTab) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            BgTab.entries.forEach { tab ->
                val isActive = tab == active
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            when (tab) {
                                BgTab.Minions -> R.string.bg_tab_minions
                                BgTab.Heroes -> R.string.bg_tab_heroes
                            },
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isActive) DeckBuilderColors.OnSurface else DeckBuilderColors.OnSurfaceDim,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(2.dp)
                            .background(
                                if (isActive) DeckBuilderColors.Primary
                                else androidx.compose.ui.graphics.Color.Transparent,
                            ),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DeckBuilderColors.OutlineSoft),
        )
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text("Search", color = DeckBuilderColors.OnSurfaceDimmer) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        leadingIcon = {
            Icon(Icons.Outlined.Search, null, tint = DeckBuilderColors.OnSurfaceDim)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = DeckBuilderColors.OnSurfaceDim,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onChange("") }
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun TierChips(selected: Set<String>, onToggle: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (1..6).forEach { tier ->
            val key = tier.toString()
            val active = key in selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) androidx.compose.ui.graphics.Color(0x33FFB454)
                        else DeckBuilderColors.SurfaceContainer,
                    )
                    .border(
                        1.dp,
                        if (active) DeckBuilderColors.Secondary else DeckBuilderColors.OutlineSoft,
                        RoundedCornerShape(8.dp),
                    )
                    .clickable { onToggle(key) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) DeckBuilderColors.Secondary else DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
    }
}

@Composable
private fun MinionTypeChips(selected: Set<String>, onToggle: (String) -> Unit) {
    val types = remember {
        listOf(
            "beast" to "Beast",
            "demon" to "Demon",
            "dragon" to "Dragon",
            "elemental" to "Elemental",
            "mech" to "Mech",
            "murloc" to "Murloc",
            "naga" to "Naga",
            "pirate" to "Pirate",
            "quilboar" to "Quilboar",
            "undead" to "Undead",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        types.forEach { (slug, label) ->
            val active = slug in selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainer,
                    )
                    .border(
                        1.dp,
                        if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                        RoundedCornerShape(99.dp),
                    )
                    .clickable { onToggle(slug) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
    }
}

@Composable
private fun CardGrid(
    state: BattlegroundsState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onCardClick: (Card) -> Unit,
) {
    val columns = if (state.tab == BgTab.Heroes) 3 else 2
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.cards, key = { it.id }) { card ->
            Box {
                CardThumbnail(card = card, onClick = { onCardClick(card) })
                val tier = card.battlegrounds?.tier
                if (tier != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeckBuilderColors.Secondary)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "T$tier",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeckBuilderColors.OnPrimary,
                        )
                    }
                }
            }
        }
        if (state.isLoadingMore || state.hasMore) {
            item(span = { GridItemSpan(columns) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyState(tab: BgTab) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(
                if (tab == BgTab.Heroes) R.string.bg_empty_heroes else R.string.bg_empty_minions,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = DeckBuilderColors.Error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(stringResource(R.string.action_retry), color = DeckBuilderColors.OnPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
