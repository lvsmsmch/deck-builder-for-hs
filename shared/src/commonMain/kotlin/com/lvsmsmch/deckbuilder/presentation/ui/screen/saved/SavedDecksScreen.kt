package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckStatsDialogForCode
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.formatColor
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroTile
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val WarningYellow = Color(0xFFE0A23F)

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

    Scaffold(
        containerColor = DeckBuilderColors.Surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChooser = true },
                containerColor = DeckBuilderColors.OnSurface,
                contentColor = DeckBuilderColors.Surface,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.new_deck_title),
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Header()
            if (state.decks.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    items(state.decks, key = { it.code }) { deck ->
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

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.saved_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SavedDeckRow(
    deck: DeckPreview,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onInfo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val classColor = colorForClassSlug(deck.classSlug)
    val incompleteCount = (deck.maxCardCount - deck.cardCount).takeIf { it > 0 }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(classColor),
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                HeroTile(
                    cardId = DefaultHeroes.cardIdFor(deck.classSlug) ?: deck.heroSlug,
                    contentDescription = deck.className,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = DeckBuilderColors.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    FormatChip(deck.format)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${classLabel(deck.classSlug)} · ${deck.cardCount}/${deck.maxCardCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.OnSurfaceDim,
                    )
                }
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
                        tint = DeckBuilderColors.OnSurface,
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
        if (incompleteCount != null) {
            Spacer(Modifier.height(6.dp))
            DeckWarning(
                text = stringResource(Res.string.deck_warning_incomplete, deck.cardCount, deck.maxCardCount),
                modifier = Modifier.padding(start = 4.dp),
            )
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
fun DeckWarning(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(WarningYellow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(12.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = WarningYellow,
        )
    }
}

@Composable
private fun FormatChip(format: GameFormat) {
    val color = formatColor(format)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = formatLabel(format),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}


@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.saved_empty_title),
            style = MaterialTheme.typography.titleMedium,
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
