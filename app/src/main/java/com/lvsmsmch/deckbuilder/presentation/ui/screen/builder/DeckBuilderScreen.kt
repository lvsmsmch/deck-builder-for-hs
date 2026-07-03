package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardPreviewDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckCardRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroPortrait
import com.lvsmsmch.deckbuilder.presentation.ui.components.ManaCurve
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.FilterSheet
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeckBuilderScreen(
    editCode: String? = null,
    savedName: String? = null,
    onDeckSaved: (String) -> Unit,
    onExit: () -> Unit,
    onOpenCard: (Card) -> Unit,
    viewModel: DeckBuilderViewModel = koinViewModel(parameters = { parametersOf(editCode, savedName) }),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showExitConfirm by remember { mutableStateOf(false) }
    var showIncompleteSaveConfirm by remember { mutableStateOf(false) }
    var rememberIncompleteSaveChoice by rememberSaveable { mutableStateOf(false) }
    var skipIncompleteSaveConfirm by rememberSaveable { mutableStateOf(false) }
    var rememberExitChoice by rememberSaveable { mutableStateOf(false) }
    var skipExitConfirm by rememberSaveable { mutableStateOf(false) }
    val requestExit = {
        if (state.phase == Phase.Editing && !skipExitConfirm) showExitConfirm = true else onExit()
    }
    val requestSave = {
        if (state.cardCount < state.maxDeckSize && !skipIncompleteSaveConfirm) {
            showIncompleteSaveConfirm = true
        } else {
            viewModel.save()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface).statusBarsPadding()) {
        when (state.phase) {
            Phase.ClassPicker -> ClassPickerView(
                slugs = CardLabels.ClassOrder,
                onPick = viewModel::pickClassBySlug,
            )
            Phase.Editing -> EditingView(
                state = state,
                onBack = requestExit,
                onSetQuery = viewModel::setPoolQuery,
                onAdd = { viewModel.addCard(it) },
                onRemove = viewModel::removeCard,
                onLoadMore = viewModel::loadNextPoolPage,
                onSave = requestSave,
                onSelectFormat = viewModel::setFormat,
                onSetPoolSort = viewModel::setPoolSort,
                onApplyPoolFilters = viewModel::applyPoolFilters,
                onOpenCard = onOpenCard,
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

    BackHandler { requestExit() }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(R.string.builder_exit_title)) },
            text = {
                Column {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.builder_exit_message))
                            if (state.cardCount < state.maxDeckSize) {
                                append(" ")
                                append(stringResource(R.string.builder_current_count_prefix))
                                append(" ")
                                withStyle(SpanStyle(color = DeckBuilderColors.Error, fontWeight = FontWeight.Bold)) {
                                    append("${state.cardCount}/${state.maxDeckSize}")
                                }
                                append(" ")
                                append(stringResource(R.string.cards_label_lowercase))
                                append(".")
                            }
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val next = !rememberExitChoice
                                rememberExitChoice = next
                                skipExitConfirm = next
                            }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rememberExitChoice,
                            onCheckedChange = {
                                rememberExitChoice = it
                                skipExitConfirm = it
                            },
                        )
                        Text(
                            text = stringResource(R.string.action_remember_choice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeckBuilderColors.OnSurface,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    if (rememberExitChoice) skipExitConfirm = true
                    onExit()
                }) { Text(stringResource(R.string.builder_exit_confirm), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }

    if (showIncompleteSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showIncompleteSaveConfirm = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(R.string.builder_incomplete_save_title)) },
            text = {
                Column {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.builder_incomplete_save_prefix))
                            append(" ")
                            withStyle(SpanStyle(color = DeckBuilderColors.Error, fontWeight = FontWeight.Bold)) {
                                append("${state.cardCount}/${state.maxDeckSize}")
                            }
                            append(" ")
                            append(stringResource(R.string.cards_label_lowercase))
                            append(". ")
                            append(stringResource(R.string.builder_incomplete_save_suffix))
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val next = !rememberIncompleteSaveChoice
                                rememberIncompleteSaveChoice = next
                                skipIncompleteSaveConfirm = next
                            }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rememberIncompleteSaveChoice,
                            onCheckedChange = {
                                rememberIncompleteSaveChoice = it
                                skipIncompleteSaveConfirm = it
                            },
                        )
                        Text(
                            text = stringResource(R.string.action_remember_choice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeckBuilderColors.OnSurface,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showIncompleteSaveConfirm = false
                    if (rememberIncompleteSaveChoice) skipIncompleteSaveConfirm = true
                    viewModel.save()
                }) { Text(stringResource(R.string.builder_incomplete_save_confirm), color = DeckBuilderColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showIncompleteSaveConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
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
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        HeroPortrait(
            cardId = DefaultHeroes.cardIdFor(slug),
            fallbackTint = Brush.linearGradient(listOf(color, DeckBuilderColors.SurfaceContainer)),
            contentDescription = classLabel(slug),
            modifier = Modifier.matchParentSize(),
            zoomed = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.5f to androidx.compose.ui.graphics.Color.Transparent,
                        1f to androidx.compose.ui.graphics.Color(0xCC000000),
                    ),
                ),
        )
        Text(
            text = classLabel(slug),
            style = MaterialTheme.typography.titleSmall,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        )
    }
}

@Composable
private fun EditingView(
    state: BuilderState,
    onBack: () -> Unit,
    onSetQuery: (String) -> Unit,
    onAdd: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onRemove: (com.lvsmsmch.deckbuilder.domain.entities.Card) -> Unit,
    onLoadMore: () -> Unit,
    onSave: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
    onSetPoolSort: (SortKey, SortDir) -> Unit,
    onApplyPoolFilters: (CardFilters) -> Unit,
    onOpenCard: (Card) -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(EditingTab.Pool) }
    val poolGridState = rememberLazyGridState()
    var showFilters by remember { mutableStateOf(false) }
    var previewCard by remember { mutableStateOf<Card?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            chosenClass = state.chosenClass,
            format = state.format,
            onBack = onBack,
            onSelectFormat = onSelectFormat,
        )

        TabBar(
            active = activeTab,
            poolCount = state.pool.totalCount,
            poolLoading = state.pool.isLoading && state.pool.totalCount == 0,
            deckCount = state.cardCount,
            maxDeckSize = state.maxDeckSize,
            onSelect = { activeTab = it },
        )

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                EditingTab.Pool -> PoolPane(
                    state = state,
                    gridState = poolGridState,
                    onSetQuery = onSetQuery,
                    onAdd = onAdd,
                    onRemove = onRemove,
                    onLoadMore = onLoadMore,
                    onSetSort = onSetPoolSort,
                    onOpenFilters = { showFilters = true },
                    onPreviewCard = { previewCard = it },
                )
                EditingTab.Deck -> DeckPane(
                    state = state,
                    onRemove = onRemove,
                    onOpenCard = onOpenCard,
                    onPreviewCard = { previewCard = it },
                )
            }
        }

        if (activeTab == EditingTab.Deck) {
            BottomActions(
                canSave = state.canSave,
                isSaving = state.isSaving,
                error = state.saveError,
                cardCount = state.cardCount,
                maxDeckSize = state.maxDeckSize,
                onSave = onSave,
            )
        }
    }

    if (showFilters) {
        FilterSheet(
            current = state.pool.filters,
            onChange = onApplyPoolFilters,
            onDismiss = { showFilters = false },
            classScopeLabel = state.chosenClass?.slug?.let { classLabel(it) },
            showFormatSection = false,
        )
    }

    previewCard?.let { card ->
        CardPreviewDialog(
            card = card,
            onDismiss = { previewCard = null },
            onMore = { onOpenCard(card) },
        )
    }
}

private enum class EditingTab { Pool, Deck }

@Composable
private fun Header(
    chosenClass: ClassMeta?,
    format: GameFormat,
    onBack: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
) {
    val color = colorForClassSlug(chosenClass?.slug)
    val selectedFormatColor = formatColor(format)
    var formatMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        HeroPortrait(
            cardId = DefaultHeroes.cardIdFor(chosenClass?.slug),
            fallbackTint = Brush.linearGradient(listOf(color, DeckBuilderColors.SurfaceContainer)),
            contentDescription = chosenClass?.let { classLabel(it.slug) },
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (chosenClass != null) classLabel(chosenClass.slug) else "Deck",
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
            )
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { formatMenuOpen = true }
                        .padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatLabel(format),
                        style = MaterialTheme.typography.bodySmall,
                        color = selectedFormatColor,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "\u25BE",
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
                            text = { Text(formatLabel(f)) },
                            onClick = {
                                onSelectFormat(f)
                                formatMenuOpen = false
                            },
                        )
                    }
                }
            }
        }

    }
}

private fun formatColor(format: GameFormat): Color = when (format) {
    GameFormat.STANDARD -> Color(0xFF3E8BFF)
    GameFormat.WILD -> Color(0xFFE09F3E)
    GameFormat.TWIST -> Color(0xFF9B6CFF)
    GameFormat.CLASSIC -> Color(0xFF5EC28A)
    GameFormat.UNKNOWN -> Color(0xFF8B929C)
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    badge: String?,
    content: @Composable () -> Unit,
) {
    Box {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
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
                )
            }
        }
    }
}

@Composable
private fun TabBar(
    active: EditingTab,
    poolCount: Int,
    poolLoading: Boolean,
    deckCount: Int,
    maxDeckSize: Int,
    onSelect: (EditingTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        TabButton(
            label = stringResource(R.string.builder_pool_tab),
            count = if (poolLoading) "..." else poolCount.toString(),
            active = active == EditingTab.Pool,
            onClick = { onSelect(EditingTab.Pool) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            label = stringResource(R.string.builder_deck_tab),
            count = "$deckCount/$maxDeckSize",
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
    count: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (active) DeckBuilderColors.OnSurface else DeckBuilderColors.OnSurfaceDim
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DeckBuilderColors.SurfaceContainerHigh)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
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
    gridState: LazyGridState,
    onSetQuery: (String) -> Unit,
    onAdd: (Card) -> Unit,
    onRemove: (Card) -> Unit,
    onLoadMore: () -> Unit,
    onSetSort: (SortKey, SortDir) -> Unit,
    onOpenFilters: () -> Unit,
    onPreviewCard: (Card) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var seenContentVersion by remember { mutableStateOf(state.pool.contentVersion) }
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
    LaunchedEffect(state.pool.contentVersion) {
        if (state.pool.contentVersion != seenContentVersion) {
            seenContentVersion = state.pool.contentVersion
            gridState.scrollToItem(0)
        }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.isScrollInProgress }.distinctUntilChanged().collect { scrolling ->
            if (scrolling) focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        PoolSearchRow(
            value = state.pool.filters.textQuery,
            onValueChange = onSetQuery,
        )

        PoolControls(
            sort = state.pool.filters.sort,
            activeFilterCount = state.pool.activeFilterCount,
            onOpenFilters = {
                focusManager.clearFocus()
                onOpenFilters()
            },
            onSortChange = {
                focusManager.clearFocus()
                onSetSort(it.key, it.direction)
            },
        )

        Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            if (state.pool.isLoading && state.pool.cards.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeckBuilderColors.Primary,
                    trackColor = DeckBuilderColors.PrimarySoft,
                )
            }
        }

        if (state.pool.isLoading && state.pool.cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
            }
            return
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.pool.cards, key = { it.id }) { card ->
                val count = state.deck[card.id]?.count ?: 0
                PoolCard(
                    card = card,
                    count = count,
                    onAdd = { onAdd(card) },
                    onRemove = { onRemove(card) },
                    onPreview = { onPreviewCard(card) },
                )
            }

            if (state.pool.isLoadingMore || state.pool.hasMore) {
                item(span = { GridItemSpan(4) }) {
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
private fun PoolCard(
    card: Card,
    count: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onPreview: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = if (count > 0) {
            Modifier
                .clip(shape)
                .border(1.dp, DeckBuilderColors.Primary, shape)
        } else {
            Modifier
        },
    ) {
        CardThumbnail(card = card, onClick = onAdd, onLongClick = onPreview)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DeckBuilderColors.Primary)
                    .border(1.dp, DeckBuilderColors.OnPrimary.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "x$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeckBuilderColors.OnPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 37.dp, end = 5.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DeckBuilderColors.SurfaceContainer.copy(alpha = 0.92f))
                    .border(1.dp, DeckBuilderColors.OutlineSoft, CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = stringResource(R.string.action_remove_card),
                    tint = DeckBuilderColors.OnSurface,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun PoolSearchRow(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.library_search_hint), color = DeckBuilderColors.OnSurfaceDimmer) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = DeckBuilderColors.OnSurfaceDim) },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_clear),
                        tint = DeckBuilderColors.OnSurfaceDim,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onValueChange("") }
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
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PoolControls(
    sort: CardSort,
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    onSortChange: (CardSort) -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(
            onClick = onOpenFilters,
            badge = activeFilterCount.takeIf { it > 0 }?.toString(),
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = stringResource(R.string.filters_title),
                tint = DeckBuilderColors.OnSurface,
                modifier = Modifier.size(21.dp),
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(8.dp))
                .clickable { sortMenuOpen = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(poolSortLabel(sort)),
                color = DeckBuilderColors.OnSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Outlined.Sort,
                contentDescription = null,
                tint = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = sortMenuOpen,
            onDismissRequest = { sortMenuOpen = false },
        ) {
            PoolSortChoices.forEach { choice ->
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
}

@Composable
private fun ManaFilterChips(
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (0..7).forEach { cost ->
            val active = cost in selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) DeckBuilderColors.PrimarySoft else DeckBuilderColors.SurfaceContainer)
                    .border(
                        1.dp,
                        if (active) DeckBuilderColors.Primary else DeckBuilderColors.OutlineSoft,
                        RoundedCornerShape(8.dp),
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

private data class PoolSortChoice(val labelRes: Int, val sort: CardSort)

private val PoolSortChoices = listOf(
    PoolSortChoice(R.string.sort_mana_asc, CardSort(SortKey.MANA_COST, SortDir.ASC)),
    PoolSortChoice(R.string.sort_mana_desc, CardSort(SortKey.MANA_COST, SortDir.DESC)),
    PoolSortChoice(R.string.sort_name, CardSort(SortKey.NAME, SortDir.ASC)),
    PoolSortChoice(R.string.sort_newest, CardSort(SortKey.DATE_ADDED, SortDir.ASC)),
    PoolSortChoice(R.string.sort_oldest, CardSort(SortKey.DATE_ADDED, SortDir.DESC)),
)

private fun poolSortLabel(sort: CardSort): Int =
    PoolSortChoices.firstOrNull { it.sort == sort }?.labelRes ?: R.string.sort_mana_asc

@Composable
private fun DeckPane(
    state: BuilderState,
    onRemove: (Card) -> Unit,
    onOpenCard: (Card) -> Unit,
    onPreviewCard: (Card) -> Unit,
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

    LazyColumn(
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeckBuilderColors.SurfaceContainer),
            ) {
                ManaCurve(entries = state.deckEntries)
            }
        }
        items(state.deckEntries, key = { it.card.id }) { entry ->
            DeckCardRow(
                entry = entry,
                onClick = { onOpenCard(entry.card) },
                onLongClick = { onPreviewCard(entry.card) },
                onRemove = { onRemove(entry.card) },
            )
        }
    }
}

@Composable
private fun BottomActions(
    canSave: Boolean,
    isSaving: Boolean,
    error: String?,
    cardCount: Int,
    maxDeckSize: Int,
    onSave: () -> Unit,
) {
    val incomplete = cardCount < maxDeckSize
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
    ) {
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = DeckBuilderColors.Error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Row {
            Button(
                onClick = onSave,
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (incomplete) DeckBuilderColors.Error else DeckBuilderColors.Primary,
                    contentColor = DeckBuilderColors.OnPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DeckBuilderColors.OnPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("${stringResource(R.string.action_save_deck)} $cardCount/$maxDeckSize")
                }
            }
        }
    }
}
