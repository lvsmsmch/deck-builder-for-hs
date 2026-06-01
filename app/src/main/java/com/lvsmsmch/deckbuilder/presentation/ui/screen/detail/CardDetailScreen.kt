package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.Keyword
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.components.rarityColor
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.labels.CardLabels
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.keywordLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.rarityLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.typeLabel
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDetailScreen(
    idOrSlug: String,
    onBack: () -> Unit,
    onCardClick: (Card) -> Unit = {},
    viewModel: CardDetailViewModel = koinViewModel(parameters = { parametersOf(idOrSlug) }),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckBuilderColors.Surface)
            .statusBarsPadding(),
    ) {
        TopBar(
            title = (state.card as? UiState.Loaded)?.data?.name ?: "",
            onBack = onBack,
        )

        when (val cardState = state.card) {
            UiState.Idle, UiState.Loading -> CardLoadingShell()

            is UiState.Failed -> ErrorState(
                message = cardState.throwable.message ?: cardState.throwable.javaClass.simpleName,
                onRetry = viewModel::load,
            )

            is UiState.Loaded -> Body(
                card = cardState.data,
                isStandardLegal = state.isStandardLegal,
                related = state.relatedCards,
                isLoadingRelated = state.isLoadingRelated,
                onRelatedClick = onCardClick,
            )
        }
    }
}

@Composable
private fun CardLoadingShell() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(18.dp)),
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DeckBuilderColors.SurfaceContainerHigh),
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeckBuilderColors.SurfaceContainer),
        )
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
            style = MaterialTheme.typography.titleSmall,
            color = DeckBuilderColors.OnSurfaceDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Body(
    card: Card,
    isStandardLegal: Boolean?,
    related: List<Card>,
    isLoadingRelated: Boolean,
    onRelatedClick: (Card) -> Unit,
) {
    val rarityColor = rarityColor(card.rarity)
    val isLegendary = card.rarity?.slug?.equals("legendary", ignoreCase = true) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(18.dp)),
        ) {
            val img = card.image.takeIf { it.isNotBlank() }
                ?.replace("/256x/", "/512x/")
                ?: card.cropImage
            if (!img.isNullOrBlank()) {
                AsyncImage(
                    model = img,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            if (isLegendary) {
                Text(
                    text = "★",
                    color = DeckBuilderColors.Rarity.Legendary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = DeckBuilderColors.OnSurface,
                    modifier = Modifier.weight(1f),
                )
                if (card.rarity != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(rarityColor),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = rarityLabel(card.rarity.slug),
                        style = MaterialTheme.typography.labelMedium,
                        color = rarityColor,
                    )
                }
            }
            SubtitleRow(card, isStandardLegal)
            Spacer(Modifier.height(14.dp))
            StatsRow(card)
            Spacer(Modifier.height(14.dp))
            CardTextBlock(card.text)
            val visibleKeywords = card.keywords.filter { CardLabels.keywordRes(it.slug) != null }
            if (visibleKeywords.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                KeywordsRow(visibleKeywords)
            }
            Spacer(Modifier.height(8.dp))
            FlavorTextBlock(card.flavorText)
        }

        if (related.isNotEmpty() || isLoadingRelated) {
            Spacer(Modifier.height(20.dp))
            RelatedCardsSection(
                cards = related,
                isLoading = isLoadingRelated,
                onClick = onRelatedClick,
            )
        }
    }
}

@Composable
private fun SubtitleRow(card: Card, isStandardLegal: Boolean?) {
    val localizedClass = card.classes.firstOrNull()?.let { classLabel(it.slug) }
    val localizedType = card.cardType.slug.takeIf { it.isNotBlank() }?.let { typeLabel(it) }
    val set = card.cardSet?.name
    val format = isStandardLegal?.let { formatLabel(if (it) GameFormat.STANDARD else GameFormat.WILD) }
    val artist = card.artistName?.let { stringResource(R.string.card_detail_by_artist, it) }
    val parts = listOfNotNull(localizedClass, localizedType, set, format, artist)
    if (parts.isEmpty()) return
    Text(
        text = parts.joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = DeckBuilderColors.OnSurfaceDim,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun StatsRow(card: Card) {
    val isWeapon = card.cardType.slug.equals("weapon", ignoreCase = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatPill(
            value = card.manaCost,
            label = stringResource(R.string.card_stat_mana),
            modifier = Modifier.weight(1f),
        )
        card.attack?.let {
            StatPill(value = it, label = stringResource(R.string.card_stat_attack), modifier = Modifier.weight(1f))
        }
        card.health?.takeUnless { isWeapon }?.let {
            StatPill(value = it, label = stringResource(R.string.card_stat_health), modifier = Modifier.weight(1f))
        }
        (if (isWeapon) card.health else card.durability)?.let {
            StatPill(value = it, label = stringResource(R.string.card_stat_durability), modifier = Modifier.weight(1f))
        }
        card.armor?.let {
            StatPill(value = it, label = stringResource(R.string.card_stat_armor), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatPill(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = DeckBuilderColors.OnSurface,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
        )
    }
}

@Composable
private fun CardTextBlock(text: String?) {
    if (text.isNullOrBlank()) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(
            text = cardTextToAnnotated(text),
            style = MaterialTheme.typography.bodyMedium,
            color = DeckBuilderColors.OnSurface,
        )
    }
}

@Composable
private fun FlavorTextBlock(text: String?) {
    if (text.isNullOrBlank()) return
    Text(
        text = cardTextToAnnotated(text),
        style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
        ),
        color = DeckBuilderColors.OnSurfaceDim,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordsRow(keywords: List<Keyword>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        keywords.forEach { keyword ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(DeckBuilderColors.SurfaceContainerHigh)
                    .border(1.dp, DeckBuilderColors.Outline, RoundedCornerShape(99.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = keywordLabel(keyword.slug, keyword.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = DeckBuilderColors.OnSurface,
                )
            }
        }
    }
}

@Composable
private fun RelatedCardsSection(
    cards: List<Card>,
    isLoading: Boolean,
    onClick: (Card) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(R.string.card_detail_related),
            style = MaterialTheme.typography.labelSmall,
            color = DeckBuilderColors.OnSurfaceDim,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            cards.forEach { c ->
                Box(modifier = Modifier.width(96.dp)) {
                    CardThumbnail(card = c, onClick = { onClick(c) })
                }
            }
            if (isLoading && cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                ) {
                    CircularProgressIndicator(
                        color = DeckBuilderColors.Primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
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
        Text(
            text = message,
            color = DeckBuilderColors.Error,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DeckBuilderColors.Primary)
                .clickable { onRetry() }
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(R.string.action_retry),
                color = DeckBuilderColors.OnPrimary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

