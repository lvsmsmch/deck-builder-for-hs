package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.components.ActionBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.ArtShard
import com.lvsmsmch.deckbuilder.presentation.ui.components.CurveSpark
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckStatsDialogForCode
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.GhostButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroTile
import com.lvsmsmch.deckbuilder.presentation.ui.components.PrimaryButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.QuietButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenHeader
import com.lvsmsmch.deckbuilder.presentation.ui.components.TagChip
import com.lvsmsmch.deckbuilder.presentation.ui.components.classGradient
import com.lvsmsmch.deckbuilder.presentation.ui.components.formatColor
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedDecksScreen(
    onOpenDeck: (String, String?) -> Unit,
    onEditDeck: (String, String?) -> Unit,
    onCreateFromScratch: () -> Unit,
    viewModel: SavedDecksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    val snackbar: SnackbarController = koinInject()
    var showChooser by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DeckPreview?>(null) }
    var statsCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.navEffects.collect { effect ->
            when (effect) {
                is SavedDecksViewModel.NavEffect.OpenDeck -> {
                    showImportSheet = false
                    showChooser = false
                    onOpenDeck(effect.code, null)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        ScreenHeader(
            title = stringResource(Res.string.saved_title),
            subtitle = if (state.decks.isEmpty()) {
                null
            } else {
                stringResource(Res.string.decks_footer_summary, state.decks.size, state.totalCards)
            },
            trailing = {
                if (state.decks.isNotEmpty()) {
                    SortControl(sort = state.sort, onSortChange = viewModel::setSort)
                }
            },
        )
        if (state.decks.isEmpty()) {
            EmptyState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.sortedDecks, key = { it.code }) { deck ->
                    SavedDeckRow(
                        deck = deck,
                        onClick = { onOpenDeck(deck.code, deck.name) },
                        onCopy = {
                            clipboard.setText(AnnotatedString(deck.code))
                            snackbar.show(UiText.of(Res.string.deck_view_copied))
                        },
                        onInfo = { statsCode = deck.code },
                        onEdit = { onEditDeck(deck.code, deck.name) },
                        onDelete = { pendingDelete = deck },
                    )
                }
            }
        }
        ActionBar(applyNavigationInset = false) {
            if (state.decks.isEmpty()) {
                QuietButton(
                    text = stringResource(Res.string.action_paste),
                    onClick = {
                        val clipboardCode = clipboard.getText()?.text.orEmpty().trim()
                            .takeIf(::looksLikeDeckCode)
                        if (clipboardCode != null) viewModel.import(clipboardCode) else showImportSheet = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            PrimaryButton(
                text = stringResource(Res.string.new_deck_title),
                onClick = { showChooser = true },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showChooser) {
        NewDeckSheet(
            onDismiss = { showChooser = false },
            onCreateFromScratch = {
                showChooser = false
                onCreateFromScratch()
            },
            onPasteCode = {
                showChooser = false
                val clipboardCode = clipboard.getText()?.text.orEmpty().trim().takeIf(::looksLikeDeckCode)
                if (clipboardCode != null) {
                    viewModel.import(clipboardCode)
                } else {
                    showImportSheet = true
                }
            },
        )
    }

    if (showImportSheet) {
        ImportDeckSheet(
            isImporting = state.importInProgress,
            error = state.importError,
            onDismiss = {
                if (!state.importInProgress) {
                    showImportSheet = false
                    viewModel.clearImportError()
                }
            },
            onErrorDismiss = viewModel::clearImportError,
            onSubmit = viewModel::import,
        )
    }

    statsCode?.let { code ->
        DeckStatsDialogForCode(code = code, onDismiss = { statsCode = null })
    }

    pendingDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(Res.string.saved_delete_title), color = DeckBuilderColors.OnSurface) },
            text = { Text(stringResource(Res.string.saved_delete_message, deck.name), color = DeckBuilderColors.OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(deck.code)
                    pendingDelete = null
                }) { Text(stringResource(Res.string.action_delete), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

/** Sort control. Quiet by design: it changes the view, it does not act on a deck. */
@Composable
private fun SortControl(sort: DeckSort, onSortChange: (DeckSort) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        GhostButton(
            text = stringResource(sort.labelRes()),
            onClick = { menuOpen = true },
            trailingIcon = Icons.Outlined.ArrowDropDown,
        )
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            containerColor = DeckBuilderColors.SurfaceContainerHigh,
        ) {
            DeckSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.labelRes()),
                            color = if (option == sort) {
                                DeckBuilderColors.Primary
                            } else {
                                DeckBuilderColors.OnSurface
                            },
                        )
                    },
                    onClick = {
                        onSortChange(option)
                        menuOpen = false
                    },
                )
            }
        }
    }
}

private fun DeckSort.labelRes(): org.jetbrains.compose.resources.StringResource = when (this) {
    DeckSort.Updated -> Res.string.decks_sort_updated
    DeckSort.Name -> Res.string.decks_sort_name
    DeckSort.Size -> Res.string.decks_sort_size
}

/**
 * Deck row. The class is the art, cut on an angle and faded into the slab; the
 * curve shows the deck's shape before you open it, and the count says whether
 * it is finished. 78dp, hairline-separated — never a floating tile.
 */
@Composable
private fun SavedDeckRow(
    deck: DeckPreview,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onInfo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val gradient = classGradient(deck.classSlug)

    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(shape)
            .background(DeckBuilderColors.SurfaceContainerHigh)
            .border(1.dp, DeckBuilderColors.Outline, shape)
            .clickable(onClick = onClick),
    ) {
        ArtShard(
            gradient = gradient,
            modifier = Modifier.width(168.dp).fillMaxHeight(),
            // The format chip and class name run across this art, and they are
            // small — the hero steps back further here than in a card row.
            veil = 0.62f,
        ) {
            HeroTile(
                cardId = DefaultHeroes.cardIdFor(deck.classSlug) ?: deck.heroSlug,
                contentDescription = deck.className,
                modifier = Modifier.fillMaxSize(),
                verticalFocus = 0.26f,
            )
        }
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 14.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = deck.name.uppercase(),
                    style = AppType.deckName,
                    color = DeckBuilderColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FormatChip(deck.format)
                    Text(
                        text = deck.classSlug?.let { classLabel(it) } ?: deck.className.orEmpty(),
                        style = AppType.rowSub,
                        color = DeckBuilderColors.OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = "${deck.cardCount}/${deck.maxCardCount}",
                        style = AppType.mono,
                        color = if (deck.cardCount >= deck.maxCardCount) {
                            DeckBuilderColors.OnSurfaceDim
                        } else {
                            DeckBuilderColors.Primary
                        },
                    )
                }
            }
            if (deck.manaCurve.isNotEmpty()) {
                CurveSpark(counts = deck.manaCurve, barWidth = 4.dp, height = 24.dp)
            }
            Box {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { menuOpen = true }
                        .padding(8.dp),
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(Res.string.action_more),
                        tint = DeckBuilderColors.OnSurfaceDimmer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DeckActionsMenu(
                    expanded = menuOpen,
                    onDismiss = { menuOpen = false },
                    onCopy = onCopy,
                    onInfo = onInfo,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
fun DeckActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopy: (() -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        onCopy?.let { copy ->
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                text = { Text(stringResource(Res.string.action_copy_code)) },
                onClick = { onDismiss(); copy() },
            )
        }
        onInfo?.let { info ->
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                text = { Text(stringResource(Res.string.action_info)) },
                onClick = { onDismiss(); info() },
            )
        }
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            text = { Text(stringResource(Res.string.action_edit)) },
            onClick = { onDismiss(); onEdit() },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = DeckBuilderColors.Error)
            },
            text = {
                Text(stringResource(Res.string.action_delete), color = DeckBuilderColors.Error)
            },
            onClick = { onDismiss(); onDelete() },
        )
    }
}

private fun looksLikeDeckCode(input: String): Boolean {
    val codeRegex = Regex("^[A-Za-z0-9+/=]{12,}$")
    val trimmed = input.trim()
    if (trimmed.matches(codeRegex)) return true
    return trimmed
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .any { it.matches(codeRegex) }
}

@Composable
private fun FormatChip(format: GameFormat) {
    val wild = format == GameFormat.WILD
    TagChip(
        text = formatLabel(format),
        color = if (wild) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
        borderColor = if (wild) DeckBuilderColors.Secondary else DeckBuilderColors.Outline,
    )
}

/** Even with nothing to show, the motif shows up: an empty curve stands in
 *  for the deck you have not built. */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CurveSpark(
            counts = listOf(1, 3, 5, 7, 5, 3, 2, 1),
            barWidth = 7.dp,
            height = 34.dp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.saved_empty_title).uppercase(),
            style = AppType.deckName,
            color = DeckBuilderColors.OnSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.saved_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )
    }
}

/** Deck warning rail, reused by the deck screen. */
@Composable
fun DeckWarning(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(DeckBuilderColors.Primary),
        )
        Text(
            text = text,
            style = AppType.rowSub.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
            color = DeckBuilderColors.Primary,
        )
    }
}
