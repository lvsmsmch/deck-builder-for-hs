package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckCardRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.ManaCurve
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DeckBuilderScreen(
    onDeckSaved: (String) -> Unit,
    viewModel: DeckBuilderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BuilderEffect.DeckSaved -> onDeckSaved(effect.code)
            }
        }
    }
    LaunchedEffect(state.toast) {
        state.toast?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        when (state.phase) {
            Phase.ClassPicker -> ClassPickerView(
                slugs = CardLabels.ClassOrder,
                onPick = viewModel::pickClassBySlug,
            )
            Phase.Editing -> EditingView(
                state = state,
                onBack = viewModel::backToPicker,
                onSetQuery = viewModel::setPoolQuery,
                onAdd = { viewModel.addCard(it) },
                onAddTwo = { viewModel.addCard(it, count = 2) },
                onRemove = viewModel::removeCard,
                onLoadMore = viewModel::loadNextPoolPage,
                onSave = viewModel::save,
                onClear = viewModel::clearDeck,
                onToggleSize = viewModel::toggleHighlanderSize,
                onToggleSingleton = viewModel::toggleSingleton,
                onSelectFormat = viewModel::setFormat,
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data ->
            Snackbar(
                containerColor = DeckBuilderColors.SurfaceContainerHigh,
                contentColor = DeckBuilderColors.OnSurface,
            ) { Text(data.visuals.message) }
        }
    }
}

@Composable
private fun ClassPickerView(
    slugs: List<String>,
    onPick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.builder_new_deck),
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp),
        )
        Text(
            text = stringResource(R.string.builder_pick_class),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 12.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(slugs, key = { it }) { slug ->
                ClassTile(slug = slug, onClick = { onPick(slug) })
            }
        }
    }
}

@Composable
private fun ClassTile(slug: String, onClick: () -> Unit) {
    val color = colorForClassSlug(slug)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(color, DeckBuilderColors.SurfaceContainer)),
            )
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = classLabel(slug),
            style = MaterialTheme.typography.titleSmall,
            color = DeckBuilderColors.OnSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EditingView(
    state: BuilderState,
    onBack: () -> Unit,
    onSetQuery: (String) -> Unit,
    onAdd: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onAddTwo: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onRemove: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onLoadMore: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onToggleSize: () -> Unit,
    onToggleSingleton: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
) {
    var activeTab by remember { mutableStateOf(EditingTab.Deck) }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            chosenClass = state.chosenClass,
            cardCount = state.cardCount,
            maxDeckSize = state.maxDeckSize,
            singleton = state.singleton,
            format = state.format,
            onBack = onBack,
            onToggleSize = onToggleSize,
            onToggleSingleton = onToggleSingleton,
            onSelectFormat = onSelectFormat,
        )

        TabBar(
            active = activeTab,
            poolCount = state.pool.totalCount,
            deckCount = state.cardCount,
            onSelect = { activeTab = it },
        )

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                EditingTab.Pool -> PoolPane(
                    state = state,
                    onSetQuery = onSetQuery,
                    onAdd = onAdd,
                    onAddTwo = onAddTwo,
                    onLoadMore = onLoadMore,
                )
                EditingTab.Deck -> DeckPane(
                    state = state,
                    onRemove = onRemove,
                )
            }
        }

        BottomActions(
            canSave = state.canSave,
            isSaving = state.isSaving,
            error = state.saveError,
            onClear = onClear,
            onSave = onSave,
        )
    }
}

private enum class EditingTab { Pool, Deck }

@Composable
private fun Header(
    chosenClass: ClassMeta?,
    cardCount: Int,
    maxDeckSize: Int,
    singleton: Boolean,
    format: GameFormat,
    onBack: () -> Unit,
    onToggleSize: () -> Unit,
    onToggleSingleton: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
) {
    val color = colorForClassSlug(chosenClass?.slug)
    val full = cardCount >= maxDeckSize
    var formatMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(color, DeckBuilderColors.SurfaceContainer)))
                .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (chosenClass != null) classLabel(chosenClass.slug) else "Deck",
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            // Format pill with dropdown.
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { formatMenuOpen = true }
                        .padding(top = 2.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = format.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.Primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "▾",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
                DropdownMenu(
                    expanded = formatMenuOpen,
                    onDismissRequest = { formatMenuOpen = false },
                ) {
                    listOf(
                        GameFormat.STANDARD,
                        GameFormat.WILD,
                        GameFormat.TWIST,
                        GameFormat.CLASSIC,
                    ).forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.displayName) },
                            onClick = {
                                onSelectFormat(f)
                                formatMenuOpen = false
                            },
                        )
                    }
                }
            }
        }

        // Singleton (highlander) badge — small "H" chip toggling singleton mode.
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (singleton) DeckBuilderColors.Rarity.Legendary.copy(alpha = 0.18f)
                    else DeckBuilderColors.SurfaceContainerHigh,
                )
                .border(
                    1.dp,
                    if (singleton) DeckBuilderColors.Rarity.Legendary else DeckBuilderColors.OutlineSoft,
                    RoundedCornerShape(8.dp),
                )
                .clickable(onClick = onToggleSingleton)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(
                text = "★1",
                style = MaterialTheme.typography.labelMedium,
                color = if (singleton) DeckBuilderColors.Rarity.Legendary else DeckBuilderColors.OnSurfaceDim,
            )
        }

        Spacer(Modifier.width(6.dp))

        // Count chip — tap toggles 30 ↔ 40 (Renathal mode).
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (full) DeckBuilderColors.Success.copy(alpha = 0.18f)
                    else DeckBuilderColors.SurfaceContainerHigh,
                )
                .border(
                    1.dp,
                    if (full) DeckBuilderColors.Success else DeckBuilderColors.OutlineSoft,
                    RoundedCornerShape(10.dp),
                )
                .clickable(onClick = onToggleSize)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = "$cardCount / $maxDeckSize",
                style = MaterialTheme.typography.labelMedium,
                color = if (full) DeckBuilderColors.Success else DeckBuilderColors.OnSurface,
            )
        }
    }
}

@Composable
private fun TabBar(
    active: EditingTab,
    poolCount: Int,
    deckCount: Int,
    onSelect: (EditingTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        TabButton(
            label = stringResource(R.string.builder_pool_tab),
            count = poolCount,
            active = active == EditingTab.Pool,
            onClick = { onSelect(EditingTab.Pool) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            label = stringResource(R.string.builder_deck_tab),
            count = deckCount,
            active = active == EditingTab.Deck,
            onClick = { onSelect(EditingTab.Deck) },
            modifier = Modifier.weight(1f),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DeckBuilderColors.OutlineSoft),
    )
}

@Composable
private fun TabButton(
    label: String,
    count: Int,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (active) DeckBuilderColors.OnSurface else DeckBuilderColors.OnSurfaceDim,
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainerHigh,
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .background(if (active) DeckBuilderColors.Primary else androidx.compose.ui.graphics.Color.Transparent),
        )
    }
}

@Composable
private fun PoolPane(
    state: BuilderState,
    onSetQuery: (String) -> Unit,
    onAdd: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onAddTwo: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onLoadMore: () -> Unit,
) {
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
            if (atEnd) onLoadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = state.pool.textQuery,
            onValueChange = onSetQuery,
            placeholder = { Text(stringResource(R.string.builder_search_pool), color = DeckBuilderColors.OnSurfaceDimmer) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = DeckBuilderColors.OnSurfaceDim) },
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
                .padding(16.dp),
        )

        if (state.pool.isLoading && state.pool.cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
            }
            return
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.pool.cards, key = { it.id }) { card ->
                Box {
                    CardThumbnail(card = card, onClick = { onAdd(card) })
                    val count = state.deck[card.id]?.count ?: 0
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeckBuilderColors.Primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "×$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeckBuilderColors.OnPrimary,
                            )
                        }
                    }
                    // Long-press = ×2 — exposed as a small button overlay for now to avoid wrestling
                    // with `combinedClickable` on the existing thumbnail. Phase 5 polish can refine.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeckBuilderColors.Primary.copy(alpha = 0.85f))
                            .clickable { onAddTwo(card) }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "+2",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeckBuilderColors.OnPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (state.pool.isLoadingMore || state.pool.hasMore) {
                item(span = { GridItemSpan(2) }) {
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
}

@Composable
private fun DeckPane(
    state: BuilderState,
    onRemove: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
) {
    if (state.deck.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.builder_empty_deck_title),
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.builder_empty_deck_body),
                style = MaterialTheme.typography.bodyMedium,
                color = DeckBuilderColors.OnSurfaceDim,
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        ) {
            ManaCurve(entries = state.deckEntries)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.deckEntries, key = { it.card.id }) { entry ->
                DeckCardRow(
                    entry = entry,
                    onClick = { onRemove(entry.card) },
                )
            }
        }
    }
}

@Composable
private fun BottomActions(
    canSave: Boolean,
    isSaving: Boolean,
    error: String?,
    onClear: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeckBuilderColors.SurfaceContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.Error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(96.dp),
            ) { Text(stringResource(R.string.action_clear)) }

            Button(
                onClick = onSave,
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeckBuilderColors.Primary,
                    contentColor = DeckBuilderColors.OnPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DeckBuilderColors.OnPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.action_save_deck))
                }
            }
        }
    }
}
