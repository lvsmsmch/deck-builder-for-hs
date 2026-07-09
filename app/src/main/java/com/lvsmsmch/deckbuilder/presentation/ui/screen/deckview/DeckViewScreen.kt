package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardPreviewDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckGridCard
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckStatsDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroTile
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.DeckActionsMenu
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.DeckWarning
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeckViewScreen(
    code: String,
    initialSavedName: String? = null,
    onBack: () -> Unit,
    onEditDeck: () -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    viewModel: DeckViewViewModel = koinViewModel(parameters = { parametersOf(code, initialSavedName.orEmpty()) }),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface)
            .statusBarsPadding()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    focusManager.clearFocus(force = true)
                }
            },
    ) {
        when (val deckState = state.deck) {
            UiState.Idle, UiState.Loading -> {
                TopBar(title = "", onBack = onBack)
                DeckLoadingShell()
            }

            is UiState.Failed -> ErrorState(
                message = deckState.throwable.message ?: deckState.throwable.javaClass.simpleName,
                onRetry = viewModel::load,
            )

            is UiState.Loaded -> Body(
                deck = deckState.data,
                savedName = state.savedName,
                isSaved = state.isSaved,
                onRename = viewModel::rename,
                onBack = onBack,
                onEditDeck = onEditDeck,
                onDeleteDeck = {
                    viewModel.deleteSavedDeck()
                    onBack()
                },
                onCardClick = onCardClick,
                onCopyCode = { copyToClipboard(context, deckState.data.code) },
            )
        }
    }
}

@Composable
private fun DeckLoadingShell() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        )
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DeckBuilderColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Body(
    deck: Deck,
    savedName: String?,
    isSaved: Boolean,
    onRename: (String) -> Unit,
    onBack: () -> Unit,
    onEditDeck: () -> Unit,
    onDeleteDeck: () -> Unit,
    onCardClick: (Card) -> Unit,
    onCopyCode: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }
    var previewCard by remember { mutableStateOf<Card?>(null) }
    var showStatsDialog by remember { mutableStateOf(false) }
    val displayName = savedName ?: deck.hero?.name ?: deck.heroClass?.name ?: "Hero"

    Column(modifier = Modifier.fillMaxSize()) {
        DeckToolbar(
            deck = deck,
            savedName = savedName,
            onRename = onRename,
            onBack = onBack,
            menuOpen = menuOpen,
            onOpenMenu = { menuOpen = true },
            onDismissMenu = { menuOpen = false },
            onEditDeck = onEditDeck,
            onDeleteDeck = { pendingDelete = true },
            onCopyCode = {
                onCopyCode()
                menuOpen = false
            },
            onInfo = { showStatsDialog = true },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(4) }) {
                DeckWarnings(deck)
            }

            item(span = { GridItemSpan(4) }) {
                ActionsRow(
                    onEditDeck = onEditDeck,
                )
            }

            items(deck.cards, key = { it.card.id }) { entry ->
                DeckGridCard(
                    card = entry.card,
                    count = entry.count,
                    showCount = true,
                    onClick = { previewCard = entry.card },
                    onLongClick = { previewCard = entry.card },
                )
            }

            if (deck.invalidCardIds.isNotEmpty()) {
                item(span = { GridItemSpan(4) }) {
                    Text(
                        text = stringResource(R.string.deck_view_invalid_format, deck.invalidCardIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.Error,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }

    previewCard?.let { card ->
        CardPreviewDialog(
            card = card,
            onDismiss = { previewCard = null },
        )
    }

    if (showStatsDialog) {
        DeckStatsDialog(deck = deck, onDismiss = { showStatsDialog = false })
    }

    if (pendingDelete) {
        AlertDialog(
            onDismissRequest = { pendingDelete = false },
            containerColor = DeckBuilderColors.SurfaceContainer,
            title = { Text(stringResource(R.string.saved_delete_title), color = DeckBuilderColors.OnSurface) },
            text = { Text(stringResource(R.string.saved_delete_message, displayName), color = DeckBuilderColors.OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = false
                    onDeleteDeck()
                }) { Text(stringResource(R.string.action_delete), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = false }) {
                    Text(stringResource(R.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

@Composable
private fun DeckToolbar(
    deck: Deck,
    savedName: String?,
    onRename: (String) -> Unit,
    onBack: () -> Unit,
    menuOpen: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditDeck: () -> Unit,
    onDeleteDeck: () -> Unit,
    onCopyCode: () -> Unit,
    onInfo: () -> Unit,
) {
    val classSlug = deck.heroClass?.slug
    val heroCardId = DefaultHeroes.cardIdFor(classSlug)
    val displayName = savedName ?: deck.hero?.name ?: deck.heroClass?.name ?: "Hero"
    val heroClassLabel = classLabel(deck.heroClass?.slug)
    val formatColor = formatColor(deck.format)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = DeckBuilderColors.OnSurface,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(10.dp)),
        ) {
            HeroTile(
                cardId = heroCardId,
                contentDescription = deck.heroClass?.name,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            EditableTitle(
                text = displayName,
                editable = false,
                onCommit = onRename,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(formatColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = formatLabel(deck.format),
                        style = MaterialTheme.typography.labelSmall,
                        color = formatColor,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$heroClassLabel \u00B7 ${deck.cardCount}/${deck.maxCardCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
        Box {
            IconButton(onClick = onOpenMenu) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.action_more),
                    tint = DeckBuilderColors.OnSurface,
                )
            }
            DeckActionsMenu(
                expanded = menuOpen,
                onDismiss = onDismissMenu,
                onCopy = onCopyCode,
                onInfo = onInfo,
                onEdit = onEditDeck,
                onDelete = onDeleteDeck,
            )
        }
    }
}

@Composable
private fun EditableTitle(
    text: String,
    editable: Boolean,
    onCommit: (String) -> Unit,
) {
    var editing by remember(text) { mutableStateOf(false) }
    var draft by remember(text) {
        mutableStateOf(TextFieldValue(text, selection = TextRange(text.length)))
    }
    var hadFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val titleStyle = MaterialTheme.typography.titleMedium.copy(color = DeckBuilderColors.OnSurface)

    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }

    if (editing) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = false,
                maxLines = 3,
                textStyle = titleStyle,
                cursorBrush = SolidColor(DeckBuilderColors.Primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                        onCommit(draft.text)
                        editing = false
                    },
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            hadFocus = true
                        } else if (hadFocus) {
                            hadFocus = false
                            onCommit(draft.text)
                            editing = false
                        }
                    }
                    .focusRequester(focusRequester),
            )
            Spacer(Modifier.width(26.dp))
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = titleStyle,
                color = DeckBuilderColors.OnSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (editable) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.action_rename),
                    tint = DeckBuilderColors.OnSurfaceDim,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { editing = true },
                )
            }
        }
    }
}

@Composable
private fun ActionsRow(
    onEditDeck: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onEditDeck,
            colors = ButtonDefaults.buttonColors(
                containerColor = DeckBuilderColors.OnSurface,
                contentColor = DeckBuilderColors.Surface,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_edit))
        }
    }
}

@Composable
private fun DeckWarnings(deck: Deck) {
    val incomplete = (deck.maxCardCount - deck.cardCount).takeIf { it > 0 }
    if (incomplete == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        DeckWarning(
            text = stringResource(R.string.deck_warning_incomplete, deck.cardCount, deck.maxCardCount),
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
        Text(text = message, color = DeckBuilderColors.Error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(text = stringResource(R.string.action_retry), color = DeckBuilderColors.OnPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun copyToClipboard(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Hearthstone deck code", code))
    Toast.makeText(context, context.getString(R.string.deck_view_copied), Toast.LENGTH_SHORT).show()
}

private fun formatColor(format: GameFormat): Color = when (format) {
    GameFormat.STANDARD -> Color(0xFF3E8BFF)
    GameFormat.WILD -> Color(0xFFE09F3E)
    GameFormat.TWIST -> Color(0xFF9B6CFF)
    GameFormat.CLASSIC -> Color(0xFF5EC28A)
    GameFormat.UNKNOWN -> Color(0xFF8B929C)
}
