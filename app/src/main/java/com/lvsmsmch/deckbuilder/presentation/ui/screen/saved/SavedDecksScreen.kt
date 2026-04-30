package com.lvsmsmch.deckbuilder.presentation.ui.screen.saved

import android.content.Intent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.DeckPreview
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroTile
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel

private val WarningYellow = Color(0xFFE0A23F)

@Composable
fun SavedDecksScreen(
    onOpenDeck: (String) -> Unit,
    onCreateFromScratch: () -> Unit,
    viewModel: SavedDecksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showChooser by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DeckPreview?>(null) }
    var pendingRename by remember { mutableStateOf<DeckPreview?>(null) }

    LaunchedEffect(Unit) {
        viewModel.navEffects.collect { effect ->
            when (effect) {
                is SavedDecksViewModel.NavEffect.OpenDeck -> {
                    showImportSheet = false
                    showChooser = false
                    onOpenDeck(effect.code)
                }
            }
        }
    }

    Scaffold(
        containerColor = DeckBuilderColors.Surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChooser = true },
                containerColor = DeckBuilderColors.Primary,
                contentColor = DeckBuilderColors.OnPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_deck_title))
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
                        .padding(horizontal = 16.dp),
                ) {
                    items(state.decks, key = { it.code }) { deck ->
                        SavedDeckRow(
                            deck = deck,
                            onClick = { onOpenDeck(deck.code) },
                            onShare = { shareDeckCode(context, deck) },
                            onRename = { pendingRename = deck },
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
                showImportSheet = true
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

    pendingDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(R.string.saved_delete_title)) },
            text = { Text(stringResource(R.string.saved_delete_message, deck.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(deck.code)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    pendingRename?.let { deck ->
        RenameDeckDialog(
            initial = deck.name,
            onDismiss = { pendingRename = null },
            onSubmit = { newName ->
                viewModel.rename(deck.code, newName)
                pendingRename = null
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
            text = stringResource(R.string.saved_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SavedDeckRow(
    deck: DeckPreview,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val classColor = colorForClassSlug(deck.classSlug)
    val incompleteCount = (30 - deck.cardCount).takeIf { it > 0 }
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
                    cardId = deck.heroSlug ?: DefaultHeroes.cardIdFor(deck.classSlug),
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
                    FormatChip(deck.format.displayName)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${classLabel(deck.classSlug)} · ${deck.cardCount}/30",
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
                        contentDescription = stringResource(R.string.action_more),
                        tint = DeckBuilderColors.OnSurfaceDimmer,
                    )
                }
                DeckActionsMenu(
                    expanded = menuOpen,
                    onDismiss = { menuOpen = false },
                    onShare = onShare,
                    onRename = onRename,
                    onDelete = onDelete,
                )
            }
        }
        if (incompleteCount != null) {
            Spacer(Modifier.height(6.dp))
            DeckWarning(
                text = stringResource(R.string.deck_warning_incomplete, deck.cardCount, 30),
                modifier = Modifier.padding(start = 39.dp), // align under tile
            )
        }
    }
}

@Composable
private fun DeckActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
            text = { Text(stringResource(R.string.action_share)) },
            onClick = { onDismiss(); onShare() },
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            text = { Text(stringResource(R.string.action_rename)) },
            onClick = { onDismiss(); onRename() },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = DeckBuilderColors.Error)
            },
            text = {
                Text(stringResource(R.string.action_delete), color = DeckBuilderColors.Error)
            },
            onClick = { onDismiss(); onDelete() },
        )
    }
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
private fun FormatChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DeckBuilderColors.PrimarySoft)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.Primary,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.saved_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = DeckBuilderColors.OnSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.saved_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun RenameDeckDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeckBuilderColors.SurfaceContainer,
        title = { Text(stringResource(R.string.rename_deck_title)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeckBuilderColors.SurfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    textStyle = TextStyle(color = DeckBuilderColors.OnSurface),
                    cursorBrush = SolidColor(DeckBuilderColors.Primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                stringResource(R.string.rename_deck_hint),
                                color = DeckBuilderColors.OnSurfaceDimmer,
                            )
                        }
                        inner()
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(value) },
                enabled = value.trim().isNotEmpty(),
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

private fun shareDeckCode(context: android.content.Context, deck: DeckPreview) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, deck.name)
        putExtra(Intent.EXTRA_TEXT, "${deck.name}\n\n${deck.code}")
    }
    context.startActivity(Intent.createChooser(intent, deck.name))
}
