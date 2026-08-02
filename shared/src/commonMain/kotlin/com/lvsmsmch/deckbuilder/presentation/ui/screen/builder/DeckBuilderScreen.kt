package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import com.lvsmsmch.deckbuilder.presentation.platform.PlatformBackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import com.lvsmsmch.deckbuilder.presentation.resolve
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardPreviewDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardSearchRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.SortChoices
import com.lvsmsmch.deckbuilder.presentation.ui.components.SortMenuButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.formatColor
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckGridCard
import com.lvsmsmch.deckbuilder.presentation.ui.components.showAppSnackbar
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroPortrait
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.library.FilterSheet
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.builder_new_deck),
            style = MaterialTheme.typography.titleLarge,
            color = DeckBuilderColors.OnSurface,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp),
        )
        Text(
            text = stringResource(Res.string.builder_pick_class),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 12.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
    onRenameDeck: (String) -> Unit,
    onOpenCard: (Card) -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(EditingTab.Deck) }
    val poolGridState = rememberLazyGridState()
    val deckGridState = rememberLazyGridState()
    var showFilters by remember { mutableStateOf(false) }
    var previewCard by remember { mutableStateOf<Card?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
            Header(
                chosenClass = state.chosenClass,
                deckName = state.deckName,
                format = state.format,
                cardCount = state.cardCount,
                maxDeckSize = state.maxDeckSize,
                showSort = activeTab == EditingTab.Pool,
                sort = state.pool.filters.sort,
            onBack = onBack,
            onSelectFormat = onSelectFormat,
            onSetSort = onSetPoolSort,
            onRenameDeck = { showRenameDialog = true },
        )

        TabBar(
            active = activeTab,
            poolCount = state.pool.totalCount,
            poolLoading = state.pool.isLoadingFirstPage && state.pool.totalCount == 0,
            deckCount = state.cardCount,
            maxDeckSize = state.maxDeckSize,
            onSelect = { activeTab = it },
        )

        BuilderHint(
            text = stringResource(
                if (activeTab == EditingTab.Deck) Res.string.builder_hint_deck else Res.string.builder_hint_pool,
            ),
        )

        Box(modifier = Modifier.weight(1f)) {
            PoolPane(
                state = state,
                gridState = poolGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (activeTab == EditingTab.Pool) 1f else 0f)
                    .zIndex(if (activeTab == EditingTab.Pool) 1f else 0f),
                onSetQuery = onSetQuery,
                onAdd = onAdd,
                onLoadMore = onLoadMore,
                onOpenFilters = { showFilters = true },
                onPreviewCard = { previewCard = it },
            )
            DeckPane(
                state = state,
                gridState = deckGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (activeTab == EditingTab.Deck) 1f else 0f)
                    .zIndex(if (activeTab == EditingTab.Deck) 1f else 0f),
                onRemove = onRemove,
                onOpenCard = { previewCard = it },
                onOpenPool = { activeTab = EditingTab.Pool },
            )
        }

        BottomActions(
            canSave = state.canSave,
            isSaving = state.isSaving,
            error = state.saveError,
            cardCount = state.cardCount,
            maxDeckSize = state.maxDeckSize,
            onSave = onSave,
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
            classScopeLabel = state.chosenClass?.slug?.let { classLabel(it) },
            showFormatSection = false,
            showClassSection = false,
        )
    }

    previewCard?.let { card ->
        CardPreviewDialog(
            card = card,
            onDismiss = { previewCard = null },
        )
    }
}

private enum class EditingTab { Pool, Deck }

@Composable
private fun BuilderHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.PrimarySoft)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = DeckBuilderColors.Primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = DeckBuilderColors.Primary,
        )
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
    val color = colorForClassSlug(chosenClass?.slug)
    val selectedFormatColor = formatColor(format)
    val classText = chosenClass?.slug?.let { classLabel(it) }.orEmpty()
    var formatMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(Res.string.action_back),
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
                text = deckName ?: chosenClass?.let { classLabel(it.slug) } ?: "Deck",
                style = MaterialTheme.typography.titleMedium,
                color = DeckBuilderColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onRenameDeck)
                    .padding(end = 8.dp),
            )
            Spacer(Modifier.height(4.dp))
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(selectedFormatColor.copy(alpha = 0.16f))
                            .clickable { formatMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = formatLabel(format),
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedFormatColor,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$classText \u00B7 $cardCount/$maxDeckSize",
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
            label = stringResource(Res.string.builder_deck_tab),
            count = "$deckCount/$maxDeckSize",
            active = active == EditingTab.Deck,
            onClick = { onSelect(EditingTab.Deck) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            label = stringResource(Res.string.builder_pool_tab),
            count = if (poolLoading) "..." else poolCount.toString(),
            active = active == EditingTab.Pool,
            onClick = { onSelect(EditingTab.Pool) },
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
                .background(if (active) DeckBuilderColors.OnSurface else Color.Transparent),
        )
    }
}

@Composable
private fun PoolPane(
    state: BuilderState,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    onSetQuery: (String) -> Unit,
    onAdd: (Card) -> Unit,
    onLoadMore: () -> Unit,
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
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        CardSearchRow(
            query = state.pool.filters.textQuery,
            onQueryChange = onSetQuery,
            activeFilterCount = state.pool.activeFilterCount,
            onOpenFilters = {
                focusManager.clearFocus()
                onOpenFilters()
            },
            modifier = Modifier.padding(top = 16.dp),
        )

        Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            if (state.pool.isLoadingFirstPage) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeckBuilderColors.Primary,
                    trackColor = DeckBuilderColors.PrimarySoft,
                )
            }
        }

        if (state.pool.isInitialLoad) {
            return
        }

        if (state.pool.cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.library_empty_with_filters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
            return
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.pool.cards, key = { it.id }) { card ->
                val count = state.deck[card.id]?.count ?: 0
                val maxCopiesReached = count >= maxCopiesFor(card, state.singleton)
                PoolCard(
                    card = card,
                    count = count,
                    maxCopiesReached = maxCopiesReached,
                    onAdd = { onAdd(card) },
                    onPreview = { onPreviewCard(card) },
                )
            }

            if (state.pool.isLoadingMore || state.pool.hasMore) {
                item(span = { GridItemSpan(4) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = DeckBuilderColors.Primary,
                            strokeWidth = 3.dp,
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
    maxCopiesReached: Boolean,
    onAdd: () -> Unit,
    onPreview: () -> Unit,
) {
    DeckGridCard(
        card = card,
        count = count,
        showCount = count > 0,
        dimImage = maxCopiesReached,
        onClick = onAdd,
        onLongClick = onPreview,
    )
}

private fun maxCopiesFor(card: Card, singleton: Boolean): Int = when {
    singleton -> 1
    card.rarity?.slug.equals("legendary", ignoreCase = true) -> 1
    else -> 2
}




@Composable
private fun DeckPane(
    state: BuilderState,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    onRemove: (Card) -> Unit,
    onOpenCard: (Card) -> Unit,
    onOpenPool: () -> Unit,
) {
    if (state.deck.isEmpty()) {
        Box(modifier = modifier) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.builder_empty_deck_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DeckBuilderColors.OnSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.builder_empty_deck_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeckBuilderColors.OnSurface)
                    .clickable(onClick = onOpenPool),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.builder_pool_tab),
                    tint = DeckBuilderColors.Surface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(state.deckEntries, key = { it.card.id }) { entry ->
            DeckGridCard(
                card = entry.card,
                count = entry.count,
                showCount = true,
                onClick = { onRemove(entry.card) },
                onLongClick = { onOpenCard(entry.card) },
            )
        }
    }
}

@Composable
private fun BottomActions(
    canSave: Boolean,
    isSaving: Boolean,
    error: UiText?,
    cardCount: Int,
    maxDeckSize: Int,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
    ) {
        if (error != null) {
            Text(
                text = error.resolve(),
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
                    containerColor = DeckBuilderColors.OnSurface,
                    contentColor = DeckBuilderColors.Surface,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DeckBuilderColors.Surface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("${stringResource(Res.string.action_save_deck)} $cardCount/$maxDeckSize")
                }
            }
        }
    }
}
