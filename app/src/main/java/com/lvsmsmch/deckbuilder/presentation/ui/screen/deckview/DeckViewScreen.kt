package com.lvsmsmch.deckbuilder.presentation.ui.screen.deckview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckCardRow
import com.lvsmsmch.deckbuilder.presentation.ui.components.DeckStatsPanel
import com.lvsmsmch.deckbuilder.presentation.ui.components.ManaCurve
import com.lvsmsmch.deckbuilder.presentation.ui.components.colorForClassSlug
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeckViewScreen(
    code: String,
    onBack: () -> Unit,
    onCardClick: (Card) -> Unit = {},
    viewModel: DeckViewViewModel = koinViewModel(parameters = { parametersOf(code) }),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface),
    ) {
        TopBar(
            title = (state.deck as? UiState.Loaded)?.data?.let { it.heroClass?.name ?: "Deck" } ?: "",
            isSaved = state.isSaved,
            onBack = onBack,
            onToggleSave = viewModel::toggleSave,
        )

        when (val deckState = state.deck) {
            UiState.Idle, UiState.Loading -> CenteredSpinner()

            is UiState.Failed -> ErrorState(
                message = deckState.throwable.message ?: deckState.throwable.javaClass.simpleName,
                onRetry = viewModel::load,
            )

            is UiState.Loaded -> Body(
                deck = deckState.data,
                code = code,
                showStats = state.showStats,
                onToggleStats = viewModel::toggleStats,
                onCardClick = onCardClick,
                onCopyCode = { copyToClipboard(context, deckState.data.code) },
                onShare = { shareCode(context, deckState.data) },
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Outlined.ArrowBack,
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
        IconButton(onClick = onToggleSave) {
            Icon(
                imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Remove from saved" else "Save deck",
                tint = if (isSaved) DeckBuilderColors.Primary else DeckBuilderColors.OnSurface,
            )
        }
    }
}

@Composable
private fun Body(
    deck: Deck,
    code: String,
    showStats: Boolean,
    onToggleStats: () -> Unit,
    onCardClick: (Card) -> Unit,
    onCopyCode: () -> Unit,
    onShare: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { HeroHeader(deck) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCopyCode,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.action_copy_code)) }
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.action_share)) }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DeckStatsPanel(deck)
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeckBuilderColors.SurfaceContainer),
            ) {
                ManaCurve(entries = deck.cards)
            }
        }

        item {
            Text(
                text = stringResource(R.string.deck_view_cards_count, deck.cardCount),
                style = MaterialTheme.typography.labelSmall,
                color = DeckBuilderColors.OnSurfaceDim,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 6.dp),
            )
        }

        items(deck.cards, key = { it.card.id }) { entry ->
            DeckCardRow(
                entry = entry,
                onClick = { onCardClick(entry.card) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
            )
        }

        if (deck.invalidCardIds.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.deck_view_invalid_format, deck.invalidCardIds.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.Error,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(deck: Deck) {
    val classColor = colorForClassSlug(deck.heroClass?.slug)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(classColor, DeckBuilderColors.SurfaceContainer),
                    ),
                )
                .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(14.dp)),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deck.hero?.name ?: deck.heroClass?.name ?: "Hero",
                style = MaterialTheme.typography.titleLarge,
                color = DeckBuilderColors.OnSurface,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeckBuilderColors.PrimarySoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = deck.format.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = DeckBuilderColors.Primary,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${deck.heroClass?.name ?: "Neutral"} · ${deck.cardCount}/30",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeckBuilderColors.OnSurfaceDim,
                )
            }
        }
    }
}

@Composable
private fun CenteredSpinner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = DeckBuilderColors.Primary, strokeWidth = 2.dp)
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

private fun shareCode(context: Context, deck: Deck) {
    val title = deck.heroClass?.name?.let { "$it deck" } ?: "Hearthstone deck"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "${title}\n\n${deck.code}")
    }
    context.startActivity(Intent.createChooser(intent, "Share deck code"))
}
