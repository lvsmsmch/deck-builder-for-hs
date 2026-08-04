package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import com.lvsmsmch.deckbuilder.presentation.platform.PlatformBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.presentation.PendingDeckAdditions
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.presentation.resolve
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.lvsmsmch.deckbuilder.presentation.ui.components.AddChip
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardListRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.CopyCount
import com.lvsmsmch.deckbuilder.presentation.ui.components.EmptyState
import com.lvsmsmch.deckbuilder.presentation.ui.components.CurveSpark
import com.lvsmsmch.deckbuilder.presentation.ui.components.manaCurveOf
import com.lvsmsmch.deckbuilder.presentation.ui.components.ArtShard
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenHeader
import com.lvsmsmch.deckbuilder.presentation.ui.components.cardGradient
import com.lvsmsmch.deckbuilder.presentation.ui.components.classGradient
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardPreviewDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardSearchRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.SortChoices
import com.lvsmsmch.deckbuilder.presentation.ui.components.SortMenuButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.formatColor
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroPortrait
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.FilterSheet
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckProgress
import com.lvsmsmch.deckbuilder.presentation.ui.components.Hairline
import com.lvsmsmch.deckbuilder.presentation.ui.components.MicroLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.PrimaryButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.TagChip
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
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
    val snackbar: SnackbarController = koinInject()
    val pendingDeckAdditions: PendingDeckAdditions = koinInject()
    var showExitConfirm by remember { mutableStateOf(false) }
    var showIncompleteSaveConfirm by remember { mutableStateOf(false) }
    val requestExit = {
        if (state.phase == Phase.Editing && !state.skipExitConfirm) showExitConfirm = true else onExit()
    }
    val requestSave = {
        if (state.cardCount < state.maxDeckSize && !state.skipIncompleteSaveConfirm) {
            showIncompleteSaveConfirm = true
        } else {
            viewModel.save()
        }
    }

    LaunchedEffect(Unit) {
        pendingDeckAdditions.requests.collect { viewModel.addCard(it) }
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
            snackbar.show(it)
            viewModel.dismissToast()
        }
    }
    fun removeWithUndo(card: Card) {
        viewModel.removeCard(card)
        snackbar.show(
            text = UiText.of(Res.string.builder_toast_card_removed, card.name),
            actionLabel = UiText.of(Res.string.action_undo),
            onAction = { viewModel.addCard(card) },
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface).statusBarsPadding()) {
        when (state.phase) {
            Phase.Loading -> BuilderLoadingView(onBack = requestExit)
            Phase.ClassPicker -> ClassPickerView(
                slugs = CardLabels.ClassOrder,
                onPick = viewModel::pickClassBySlug,
            )
            Phase.Editing -> EditingView(
                state = state,
                onBack = requestExit,
                onSetQuery = viewModel::setPoolQuery,
                onAdd = { viewModel.addCard(it) },
                onRemove = ::removeWithUndo,
                onLoadMore = viewModel::loadNextPoolPage,
                onSave = requestSave,
                onSelectFormat = viewModel::setFormat,
                onSetPoolSort = viewModel::setPoolSort,
                onApplyPoolFilters = viewModel::applyPoolFilters,
                onRenameDeck = viewModel::renameDeck,
                onOpenCard = onOpenCard,
            )
        }

    }

    PlatformBackHandler { requestExit() }

    if (showExitConfirm) {
        var rememberExitChoice by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(Res.string.builder_exit_title), color = DeckBuilderColors.OnSurface) },
            text = {
                Column {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(Res.string.builder_exit_message))
                            if (state.cardCount < state.maxDeckSize) {
                                append(" ")
                                append(stringResource(Res.string.builder_current_count_prefix))
                                append(" ")
                                withStyle(SpanStyle(color = DeckBuilderColors.Error)) {
                                    append("${state.cardCount}/${state.maxDeckSize}")
                                }
                                append(" ")
                                append(stringResource(Res.string.cards_label_lowercase))
                                append(".")
                            }
                        },
                        color = DeckBuilderColors.OnSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    RememberChoiceRow(
                        checked = rememberExitChoice,
                        onCheckedChange = { rememberExitChoice = it },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    if (rememberExitChoice) viewModel.rememberSkipExitConfirm()
                    onExit()
                }) { Text(stringResource(Res.string.builder_exit_confirm), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }

    if (showIncompleteSaveConfirm) {
        var rememberIncompleteSaveChoice by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showIncompleteSaveConfirm = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(Res.string.builder_incomplete_save_title), color = DeckBuilderColors.OnSurface) },
            text = {
                Column {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(Res.string.builder_incomplete_save_prefix))
                            append(" ")
                            withStyle(SpanStyle(color = DeckBuilderColors.Error)) {
                                append("${state.cardCount}/${state.maxDeckSize}")
                            }
                            append(" ")
                            append(stringResource(Res.string.cards_label_lowercase))
                            append(". ")
                            append(stringResource(Res.string.builder_incomplete_save_suffix))
                        },
                        color = DeckBuilderColors.OnSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    RememberChoiceRow(
                        checked = rememberIncompleteSaveChoice,
                        onCheckedChange = { rememberIncompleteSaveChoice = it },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showIncompleteSaveConfirm = false
                    if (rememberIncompleteSaveChoice) viewModel.rememberSkipIncompleteSaveConfirm()
                    viewModel.save()
                }) { Text(stringResource(Res.string.builder_incomplete_save_confirm), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showIncompleteSaveConfirm = false }) {
                    Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

@Composable
private fun RememberChoiceRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = rememberCheckboxColors(),
        )
        Text(
            text = stringResource(Res.string.action_remember_choice),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurface,
        )
    }
}

@Composable
private fun BuilderLoadingView(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 4.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(Res.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = DeckBuilderColors.OnSurface,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ClassPickerView(
    slugs: List<String>,
    onPick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        ScreenHeader(
            title = stringResource(Res.string.builder_new_deck),
            subtitle = stringResource(Res.string.builder_pick_class),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.fillMaxSize().background(DeckBuilderColors.OutlineSoft),
        ) {
            items(slugs, key = { it }) { slug ->
                ClassTile(slug = slug, onClick = { onPick(slug) })
            }
        }
    }
}

/**
 * The same angled cut used in the deck list, so the class you pick here is
 * visibly the thing you will see there.
 */
@Composable
private fun ClassTile(slug: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(DeckBuilderColors.Surface)
            .clickable(onClick = onClick),
    ) {
        ArtShard(
            gradient = classGradient(slug),
            modifier = Modifier.fillMaxSize(),
            skew = 0.dp,
            fadeFrom = 0.45f,
        ) {
            HeroPortrait(
                cardId = DefaultHeroes.cardIdFor(slug),
                fallbackTint = Brush.linearGradient(classGradient(slug).toList()),
                contentDescription = classLabel(slug),
                modifier = Modifier.matchParentSize(),
                zoomed = true,
            )
        }
        Text(
            text = classLabel(slug).uppercase(),
            style = AppType.deckName.copy(fontSize = 15.sp),
            color = DeckBuilderColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingView(
    state: BuilderState,
    onBack: () -> Unit,
    onSetQuery: (String) -> Unit,
    onAdd: (Card) -> Unit,
    onRemove: (Card) -> Unit,
    onLoadMore: () -> Unit,
    onSave: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
    onSetPoolSort: (SortKey, SortDir) -> Unit,
    onApplyPoolFilters: (CardFilters) -> Unit,
    onRenameDeck: (String) -> Unit,
    onOpenCard: (Card) -> Unit,
) {
    val poolListState = rememberLazyListState()
    var showFilters by remember { mutableStateOf(false) }
    var showDeckSheet by remember { mutableStateOf(false) }
    var previewCard by remember { mutableStateOf<Card?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val nearEnd by remember {
        derivedStateOf {
            val total = poolListState.layoutInfo.totalItemsCount
            val lastVisible = poolListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 8
        }
    }
    LaunchedEffect(poolListState) {
        snapshotFlow { nearEnd }.distinctUntilChanged().collect { atEnd -> if (atEnd) onLoadMore() }
    }
    var seenContentVersion by remember { mutableStateOf(state.pool.contentVersion) }
    LaunchedEffect(state.pool.contentVersion) {
        if (state.pool.contentVersion != seenContentVersion) {
            seenContentVersion = state.pool.contentVersion
            poolListState.scrollToItem(0)
        }
    }
    LaunchedEffect(poolListState) {
        snapshotFlow { poolListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling -> if (scrolling) focusManager.clearFocus() }
    }

    Column(modifier = Modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        Header(
            chosenClass = state.chosenClass,
            deckName = state.deckName,
            format = state.format,
            cardCount = state.cardCount,
            maxDeckSize = state.maxDeckSize,
            showSort = true,
            sort = state.pool.filters.sort,
            onBack = onBack,
            onSelectFormat = onSelectFormat,
            onSetSort = onSetPoolSort,
            onRenameDeck = { showRenameDialog = true },
        )
        // The rule fills as the deck does — progress without a widget.
        DeckProgress(cardCount = state.cardCount, maxCardCount = state.maxDeckSize)
        Spacer(Modifier.height(12.dp))

        CardSearchRow(
            query = state.pool.filters.textQuery,
            onQueryChange = onSetQuery,
            activeFilterCount = state.pool.activeFilterCount,
            onOpenFilters = {
                focusManager.clearFocus()
                showFilters = true
            },
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.pool.isInitialLoad -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = DeckBuilderColors.Primary,
                        strokeWidth = 2.5.dp,
                    )
                }

                state.pool.cards.isEmpty() -> EmptyState(stringResource(Res.string.library_empty_with_filters))

                else -> LazyColumn(
                    state = poolListState,
                    contentPadding = PaddingValues(bottom = 12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        },
                ) {
                    items(state.pool.cards, key = { it.id }) { card ->
                        val inDeck = state.deck[card.id]?.count ?: 0
                        val maxed = inDeck >= maxCopiesFor(card, state.singleton)
                        CardListRow(
                            manaCost = card.manaCost,
                            name = card.name,
                            raritySlug = card.rarity?.slug,
                            artUrl = card.cropImage ?: card.image,
                            gradient = cardGradient(card),
                            dimmed = maxed,
                            onClick = { if (!maxed) onAdd(card) },
                            onLongClick = { previewCard = card },
                            trailing = {
                                if (inDeck > 0) CopyCount(inDeck, dimmed = maxed) else AddChip()
                            },
                        )
                    }
                    if (state.pool.isLoadingMore || state.pool.hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = DeckBuilderColors.Primary,
                                    strokeWidth = 2.5.dp,
                                )
                            }
                        }
                    }
                }
            }

            if (state.pool.isLoadingFirstPage && state.pool.cards.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                    color = DeckBuilderColors.Primary,
                    trackColor = DeckBuilderColors.PrimarySoft,
                )
            }
        }

        DeckStrip(
            state = state,
            onOpenDeck = { showDeckSheet = true },
            onSave = onSave,
        )
    }

    if (showDeckSheet) {
        DeckSheet(
            state = state,
            onDismiss = { showDeckSheet = false },
            onRemove = onRemove,
            onOpenCard = { previewCard = it },
        )
    }

    if (showRenameDialog) {
        RenameDeckDialog(
            initial = state.deckName ?: state.chosenClass?.slug?.let { classLabel(it) }.orEmpty(),
            onDismiss = { showRenameDialog = false },
            onSubmit = {
                onRenameDeck(it)
                showRenameDialog = false
            },
        )
    }

    if (showFilters) {
        FilterSheet(
            current = state.pool.filters,
            onChange = onApplyPoolFilters,
            onDismiss = { showFilters = false },
            resultCount = state.pool.totalCount,
            classScopeLabel = state.chosenClass?.slug?.let { classLabel(it) },
            showFormatSection = false,
            showClassSection = false,
        )
    }

    previewCard?.let { card ->
        CardPreviewDialog(
            card = card,
            onDismiss = { previewCard = null },
            onOpenDetails = {
                previewCard = null
                onOpenCard(card)
            },
        )
    }
}

/**
 * Pinned deck summary. The pool fills the screen while building, so this strip
 * is the deck's presence: how many cards, what shape, and the way into the full
 * list. Replaces the Deck/Pool tabs, which cost the pool's context on every
 * check of what is already in the deck.
 */
@Composable
private fun DeckStrip(
    state: BuilderState,
    onOpenDeck: () -> Unit,
    onSave: () -> Unit,
) {
    val curve = remember(state.deckEntries) { manaCurveOf(state.deckEntries) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeckBuilderColors.SurfaceContainer),
    ) {
        Hairline()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDeck)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MicroLabel(stringResource(Res.string.builder_deck_strip_label))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = state.cardCount.toString(),
                        style = AppType.figure.copy(fontSize = 22.sp),
                        color = DeckBuilderColors.Primary,
                    )
                    Text(
                        text = "/${state.maxDeckSize}",
                        style = AppType.figure.copy(fontSize = 22.sp),
                        color = DeckBuilderColors.OnSurfaceDimmer,
                    )
                }
                Spacer(Modifier.weight(1f))
                // Fixed bar width: stretched to fill, a single populated bucket
                // reads as a stray rectangle rather than a curve.
                CurveSpark(counts = curve, barWidth = 4.dp, height = 22.dp)
                Icon(
                    Icons.Outlined.KeyboardArrowUp,
                    contentDescription = null,
                    tint = DeckBuilderColors.OnSurfaceDimmer,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (state.saveError != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.saveError.resolve(),
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.Error,
                )
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                text = stringResource(Res.string.action_save_deck),
                onClick = onSave,
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The deck itself, reachable from the strip: tap a row to take a copy out. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckSheet(
    state: BuilderState,
    onDismiss: () -> Unit,
    onRemove: (Card) -> Unit,
    onOpenCard: (Card) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeckBuilderColors.Surface,
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${stringResource(Res.string.builder_deck_tab)} · ${state.cardCount}/${state.maxDeckSize}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeckBuilderColors.OnSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.builder_hint_deck),
                    style = MaterialTheme.typography.labelSmall,
                    color = DeckBuilderColors.OnSurfaceDimmer,
                )
            }
            if (state.deckEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.builder_empty_deck_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(state.deckEntries, key = { it.card.id }) { entry ->
                        CardListRow(
                            manaCost = entry.card.manaCost,
                            name = entry.card.name,
                            raritySlug = entry.card.rarity?.slug,
                            artUrl = entry.card.cropImage ?: entry.card.image,
                            gradient = cardGradient(entry.card),
                            onClick = { onRemove(entry.card) },
                            onLongClick = { onOpenCard(entry.card) },
                            trailing = { CopyCount(entry.count) },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun rememberCheckboxColors() = CheckboxDefaults.colors(
    checkedColor = DeckBuilderColors.OnSurface,
    uncheckedColor = DeckBuilderColors.OnSurface,
    checkmarkColor = DeckBuilderColors.Surface,
)

@Composable
private fun Header(
    chosenClass: ClassMeta?,
    deckName: String?,
    format: GameFormat,
    cardCount: Int,
    maxDeckSize: Int,
    showSort: Boolean,
    sort: CardSort,
    onBack: () -> Unit,
    onSelectFormat: (GameFormat) -> Unit,
    onSetSort: (SortKey, SortDir) -> Unit,
    onRenameDeck: () -> Unit,
) {
    val classText = chosenClass?.slug?.let { classLabel(it) }.orEmpty()
    var formatMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 14.dp, top = 6.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(Res.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (deckName ?: classText).uppercase(),
                style = AppType.screenTitle.copy(fontSize = 26.sp),
                color = DeckBuilderColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(onClick = onRenameDeck)
                    .padding(end = 8.dp),
            )
            Spacer(Modifier.height(6.dp))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.clickable { formatMenuOpen = true }) {
                        TagChip(
                            text = formatLabel(format),
                            color = if (format == GameFormat.WILD) {
                                DeckBuilderColors.Primary
                            } else {
                                DeckBuilderColors.OnSurfaceDim
                            },
                            borderColor = if (format == GameFormat.WILD) {
                                DeckBuilderColors.Secondary
                            } else {
                                DeckBuilderColors.Outline
                            },
                        )
                    }
                    Text(
                        text = classText,
                        style = AppType.rowSub,
                        color = DeckBuilderColors.OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(
                    expanded = formatMenuOpen,
                    onDismissRequest = { formatMenuOpen = false },
                    containerColor = DeckBuilderColors.SurfaceContainerHigh,
                ) {
                    listOf(
                        GameFormat.STANDARD,
                        GameFormat.WILD,
                        GameFormat.TWIST,
                        GameFormat.CLASSIC,
                    ).forEach { f ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = formatLabel(f),
                                    color = if (f == format) {
                                        DeckBuilderColors.Primary
                                    } else {
                                        DeckBuilderColors.OnSurface
                                    },
                                )
                            },
                            onClick = {
                                onSelectFormat(f)
                                formatMenuOpen = false
                            },
                        )
                    }
                }
            }
        }
        if (showSort) {
            SortMenuButton(
                sort = sort,
                choices = SortChoices.Pool,
                onSortChange = { onSetSort(it.key, it.direction) },
            )
        }
    }
}

@Composable
private fun RenameDeckDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val initialValue = remember(initial) { initial.take(100) }
    var value by remember(initialValue) {
        mutableStateOf(TextFieldValue(initialValue, selection = TextRange(initialValue.length)))
    }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(Res.string.rename_deck_title), color = DeckBuilderColors.OnSurface) },
        text = {
            TextField(
                value = value,
                onValueChange = { next ->
                    val clipped = next.text.take(100)
                    value = next.copy(
                        text = clipped,
                        selection = TextRange(
                            next.selection.start.coerceIn(0, clipped.length),
                            next.selection.end.coerceIn(0, clipped.length),
                        ),
                        composition = null,
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(Res.string.rename_deck_hint),
                        color = DeckBuilderColors.OnSurfaceDimmer,
                    )
                },
                singleLine = true,
                trailingIcon = {
                    if (value.text.isNotEmpty()) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(Res.string.action_clear),
                            tint = DeckBuilderColors.OnSurfaceDim,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { value = TextFieldValue("") }
                                .padding(4.dp),
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DeckBuilderColors.SurfaceContainerHigh,
                    unfocusedContainerColor = DeckBuilderColors.SurfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = DeckBuilderColors.OnSurface,
                    unfocusedTextColor = DeckBuilderColors.OnSurface,
                    cursorColor = DeckBuilderColors.OnSurface,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(value.text) },
                enabled = value.text.trim().isNotEmpty(),
            ) { Text(stringResource(Res.string.action_save), color = DeckBuilderColors.OnSurface) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
            }
        },
    )
}

/** Mirrors the cap the view model enforces, so the pool row can dim in step with it. */
private fun maxCopiesFor(card: Card, singleton: Boolean): Int = when {
    singleton -> 1
    card.rarity?.slug.equals("legendary", ignoreCase = true) -> 1
    else -> 2
}
