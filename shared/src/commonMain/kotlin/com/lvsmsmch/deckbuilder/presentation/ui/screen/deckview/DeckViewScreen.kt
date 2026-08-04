package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardListRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.CopyCount
import com.lvsmsmch.deckbuilder.presentation.ui.components.ManaCurve
import com.lvsmsmch.deckbuilder.presentation.ui.components.StatValue
import com.lvsmsmch.deckbuilder.presentation.ui.components.averageManaCost
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.components.craftingCostOf
import com.lvsmsmch.deckbuilder.presentation.ui.components.manaCurveOf
import com.lvsmsmch.deckbuilder.presentation.ui.components.cardGradient
import com.lvsmsmch.deckbuilder.util.formatFixed
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardPreviewDialog
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckGridCard
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckStatsDialog
import com.lvsmsmch.deckbuilder.presentation.resolve
import com.lvsmsmch.deckbuilder.presentation.toUiText
import com.lvsmsmch.deckbuilder.presentation.ui.components.ErrorState
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenTopBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.formatColor
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import com.lvsmsmch.deckbuilder.presentation.ui.components.HeroTile
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.DeckActionsMenu
import com.lvsmsmch.deckbuilder.presentation.ui.screen.saved.DeckWarning
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import com.lvsmsmch.deckbuilder.presentation.ui.components.ActionBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.Hairline
import com.lvsmsmch.deckbuilder.presentation.ui.components.MicroLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.NoticeRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.Backdrop
import com.lvsmsmch.deckbuilder.presentation.ui.components.GlassPane
import com.lvsmsmch.deckbuilder.presentation.ui.components.classAtmosphere
import com.lvsmsmch.deckbuilder.presentation.ui.components.PrimaryButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.QuietButton
import com.lvsmsmch.deckbuilder.presentation.ui.components.SectionLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.TagChip
import com.lvsmsmch.deckbuilder.presentation.ui.components.classGradient
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.UiText
import org.koin.compose.koinInject
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
    val clipboard = LocalClipboardManager.current
    val snackbar: SnackbarController = koinInject()
    val focusManager = LocalFocusManager.current

    // No background and no inset here: the backdrop inside runs edge to edge and
    // the screens below it take the status bar themselves.
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                message = deckState.throwable.toUiText().resolve(),
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
                onCopyCode = {
                    clipboard.setText(AnnotatedString(deckState.data.code))
                    snackbar.show(UiText.of(Res.string.deck_view_copied))
                },
            )
        }
    }
}

@Composable
private fun DeckLoadingShell() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
    ScreenTopBar(title = title, onBack = onBack, bottomPadding = 4.dp)
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

    Backdrop(
        atmosphere = classAtmosphere(deck.heroClass?.slug),
        blurRadius = 26.dp,
        scrimFrom = 0.26f,
        art = {
            HeroTile(
                cardId = DefaultHeroes.cardIdFor(deck.heroClass?.slug),
                contentDescription = deck.heroClass?.name,
                modifier = Modifier.fillMaxSize(),
                verticalFocus = 0.22f,
            )
        },
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                DeckHero(
                    deck = deck,
                    displayName = displayName,
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
            }

            item { StatBand(deck) }

            item {
                val counts = remember(deck.cards) { manaCurveOf(deck.cards) }
                val peak = remember(counts) { counts.indexOf(counts.maxOrNull() ?: 0) }
                GlassPane(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MicroLabel(stringResource(Res.string.deck_curve_title))
                        if (deck.cardCount > 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // The dashed rule in the chart is this number.
                                MicroLabel(
                                    text = "AVG " + formatFixed(
                                        remember(deck.cards) { averageManaCost(deck.cards) },
                                        1,
                                    ),
                                    color = DeckBuilderColors.Primary,
                                )
                                MicroLabel(
                                    stringResource(
                                        Res.string.deck_curve_peak,
                                        if (peak == counts.lastIndex) "7+" else peak.toString(),
                                    ),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    ManaCurve(
                        counts = counts,
                        average = remember(deck.cards) { averageManaCost(deck.cards) },
                    )
                  }
                }
            }

            item { DeckWarnings(deck) }

            item {
                SectionLabel(
                    text = "${stringResource(Res.string.deck_section_cards)} · ${deck.cardCount}",
                    trailing = stringResource(Res.string.deck_section_copies),
                )
            }

            items(deck.cards, key = { it.card.id }) { entry ->
                CardListRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    manaCost = entry.card.manaCost,
                    name = entry.card.name,
                    raritySlug = entry.card.rarity?.slug,
                    artUrl = entry.card.cropImage ?: entry.card.image,
                    gradient = cardGradient(entry.card),
                    onClick = { previewCard = entry.card },
                    onLongClick = { onCardClick(entry.card) },
                    trailing = { CopyCount(entry.count) },
                )
            }

            if (deck.invalidCardIds.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.deck_view_invalid_format, deck.invalidCardIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = DeckBuilderColors.Error,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }

        ActionBar {
            QuietButton(
                text = stringResource(Res.string.action_copy_code),
                onClick = onCopyCode,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                text = stringResource(Res.string.deck_view_edit),
                onClick = onEditDeck,
                modifier = Modifier.weight(1f),
            )
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
            title = { Text(stringResource(Res.string.saved_delete_title), color = DeckBuilderColors.OnSurface) },
            text = { Text(stringResource(Res.string.saved_delete_message, displayName), color = DeckBuilderColors.OnSurface) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = false
                    onDeleteDeck()
                }) { Text(stringResource(Res.string.action_delete), color = DeckBuilderColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = false }) {
                    Text(stringResource(Res.string.action_cancel), color = DeckBuilderColors.OnSurface)
                }
            },
        )
    }
}

/**
 * The deck's identity, at full bleed. Art runs behind the name and fades into
 * the slab, so the screen opens with the thing you recognise rather than with
 * a toolbar. Back and the overflow ride on top of it.
 */
/**
 * The deck's own name over its own art — the art is the screen's backdrop, so
 * this is only the words and the two controls that ride on top of them.
 */
@Composable
private fun DeckHero(
    deck: Deck,
    displayName: String,
    onBack: () -> Unit,
    menuOpen: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditDeck: () -> Unit,
    onDeleteDeck: () -> Unit,
    onCopyCode: () -> Unit,
    onInfo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = DeckBuilderColors.OnSurface,
                )
            }
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(onClick = onOpenMenu) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(Res.string.action_more),
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
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = displayName,
                style = AppType.heroTitle,
                color = DeckBuilderColors.OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                FormatChip(deck.format)
                TagChip(text = deck.heroClass?.slug?.let { classLabel(it) } ?: displayName)
            }
        }
    }
}

/** The three figures a player checks first, in a hairline-divided band. */
@Composable
private fun StatBand(deck: Deck) {
    GlassPane(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            StatValue(
                value = "${deck.cardCount}/${deck.maxCardCount}",
                caption = stringResource(Res.string.deck_stats_cards_short),
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp),
            )
            Box(Modifier.width(1.dp).fillMaxHeight().background(DeckBuilderColors.OutlineSoft))
            StatValue(
                value = formatFixed(remember(deck.cards) { averageManaCost(deck.cards) }, 1),
                caption = stringResource(Res.string.deck_stats_avg_mana_short),
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp),
            )
            Box(Modifier.width(1.dp).fillMaxHeight().background(DeckBuilderColors.OutlineSoft))
            StatValue(
                value = remember(deck.cards) { craftingCostOf(deck.cards) }.toString(),
                caption = stringResource(Res.string.deck_stats_dust_short),
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
    }
}

/** Format badge. Wild is the exception worth naming, so only it takes brass. */
@Composable
fun FormatChip(format: GameFormat, modifier: Modifier = Modifier) {
    val wild = format == GameFormat.WILD
    TagChip(
        text = formatLabel(format),
        modifier = modifier,
        color = if (wild) DeckBuilderColors.Primary else DeckBuilderColors.OnSurfaceDim,
        borderColor = if (wild) DeckBuilderColors.Secondary else DeckBuilderColors.Outline,
    )
}

@Composable
private fun DeckWarnings(deck: Deck) {
    val incomplete = (deck.maxCardCount - deck.cardCount).takeIf { it > 0 } ?: return
    NoticeRow(
        text = stringResource(Res.string.deck_warning_incomplete, deck.cardCount, deck.maxCardCount),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
