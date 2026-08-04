package com.lvsmsmch.deckbuilder.presentation.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.*
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.resolve
import com.lvsmsmch.deckbuilder.presentation.toUiText
import androidx.compose.foundation.border
import com.lvsmsmch.deckbuilder.presentation.ui.components.RarityDot
import com.lvsmsmch.deckbuilder.presentation.ui.labels.SetReleaseDates
import com.lvsmsmch.deckbuilder.presentation.ui.labels.expansionLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.typeLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.ErrorState
import com.lvsmsmch.deckbuilder.presentation.ui.components.ScreenTopBar
import com.lvsmsmch.deckbuilder.presentation.ui.components.rarityColor
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.rarityLabel
import com.lvsmsmch.deckbuilder.presentation.ui.labels.typeLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.ActionBar
import androidx.compose.ui.draw.drawBehind
import com.lvsmsmch.deckbuilder.presentation.ui.components.Plate
import com.lvsmsmch.deckbuilder.presentation.ui.components.MicroLabel
import com.lvsmsmch.deckbuilder.presentation.ui.components.PrimaryButton
import com.lvsmsmch.deckbuilder.presentation.ui.theme.AppType
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CardDetailScreen(
    idOrSlug: String,
    onBack: () -> Unit,
    onCardClick: (Card) -> Unit = {},
    canAddToDeck: Boolean = false,
    onAddToDeck: (Card) -> Unit = {},
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
                message = cardState.throwable.toUiText().resolve(),
                onRetry = viewModel::load,
            )

            is UiState.Loaded -> {
                Body(
                    card = cardState.data,
                    isStandardLegal = state.isStandardLegal,
                    related = state.relatedCards,
                    isLoadingRelated = state.isLoadingRelated,
                    onRelatedClick = onCardClick,
                    modifier = Modifier.weight(1f),
                )
                // Only the deck editor has a deck to add to, so the bar shows up
                // exclusively on the trip that started there.
                if (canAddToDeck) {
                    ActionBar {
                        PrimaryButton(
                            text = stringResource(Res.string.card_detail_add_to_deck),
                            onClick = { onAddToDeck(cardState.data) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
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
    ScreenTopBar(
        title = title.uppercase(),
        onBack = onBack,
        titleStyle = AppType.screenTitle.copy(fontSize = 18.sp, letterSpacing = 0.8.sp),
    )
}

@Composable
private fun Body(
    card: Card,
    isStandardLegal: Boolean?,
    related: List<Card>,
    isLoadingRelated: Boolean,
    onRelatedClick: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rarityColor = rarityColor(card.rarity)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        CardImagePanel(
            card = card,
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.name.uppercase(),
                    style = AppType.screenTitle.copy(fontSize = 22.sp),
                    color = DeckBuilderColors.OnSurface,
                    modifier = Modifier.weight(1f),
                )
                val rarity = card.rarity
                if (rarity != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(rarityColor),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = rarityLabel(rarity.slug).uppercase(),
                        style = AppType.microSmall,
                        color = rarityColor,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            CardFacts(card = card, isStandardLegal = isStandardLegal)
            Spacer(Modifier.height(10.dp))
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

private const val CARD_RENDER_ASPECT = 0.72f

@Composable
private fun CardImagePanel(
    card: Card,
) {
    val lowUrl = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val highUrl = card.image.takeIf { it.isNotBlank() }?.replace("/256x/", "/512x/") ?: card.cropImage
    val context = LocalPlatformContext.current
    val lowPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(lowUrl)
            .size(256, 384)
            .build(),
    )
    val highPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(highUrl)
            .size(512, 768)
            .build(),
    )
    val highState by highPainter.state.collectAsState()

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .aspectRatio(CARD_RENDER_ASPECT),
        ) {
            if (!lowUrl.isNullOrBlank()) {
                Image(
                    painter = lowPainter,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            if (highState is AsyncImagePainter.State.Success && !highUrl.isNullOrBlank()) {
                Image(
                    painter = highPainter,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            when (highState) {
                is AsyncImagePainter.State.Error -> CardImageErrorOverlay(onRetry = { highPainter.restart() })
                is AsyncImagePainter.State.Success -> Unit
                else -> CardImageLoadingOverlay()
            }
        }
    }
}

@Composable
private fun BoxScope.CardImageLoadingOverlay() {
    Text(
        text = stringResource(Res.string.card_image_loading),
        color = Color.White,
        style = MaterialTheme.typography.labelLarge.copy(
            shadow = Shadow(color = Color.Black, blurRadius = 6f),
        ),
        modifier = Modifier
            .align(Alignment.Center)
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun BoxScope.CardImageErrorOverlay(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .background(Color.Black.copy(alpha = 0.46f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.card_image_error),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(color = Color.Black, blurRadius = 6f),
            ),
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DeckBuilderColors.Primary)
                .clickable { onRetry() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_retry),
                color = DeckBuilderColors.OnPrimary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun FullscreenCardImage(card: Card, onDismiss: () -> Unit) {
    val highUrl = card.image.takeIf { it.isNotBlank() }?.replace("/256x/", "/512x/") ?: card.cropImage
    val fallbackUrl = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(highUrl ?: fallbackUrl)
            .size(512, 768)
            .build(),
    )
    var targetScale by remember { mutableFloatStateOf(1f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }
    var gestureActive by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 180),
        label = "fullscreen-card-scale",
    )
    val offset by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 180),
        label = "fullscreen-card-offset",
    )
    val renderedScale = if (gestureActive) targetScale else scale
    val renderedOffset = if (gestureActive) targetOffset else offset

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeckBuilderColors.Surface.copy(alpha = 0.88f))
                .statusBarsPadding(),
        ) {
            Image(
                painter = painter,
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 72.dp)
                    .fillMaxWidth()
                    .aspectRatio(CARD_RENDER_ASPECT)
                    .graphicsLayer {
                        scaleX = renderedScale
                        scaleY = renderedScale
                        translationX = renderedOffset.x
                        translationY = renderedOffset.y
                    }
                    .pointerInput(card.id) {
                        awaitEachGesture {
                            awaitFirstDown()
                            gestureActive = true
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                targetScale = (targetScale * zoom).coerceIn(1f, 3.2f)
                                if (event.changes.count { it.pressed } >= 2) {
                                    targetOffset += event.calculatePan()
                                }
                            } while (event.changes.any { it.pressed })
                            gestureActive = false
                            targetScale = 1f
                            targetOffset = Offset.Zero
                        }
                    },
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.action_close),
                    tint = DeckBuilderColors.OnSurface,
                )
            }
        }
    }
}

/**
 * The facts a player checks before crafting: who can play it, what it is, when
 * it shipped, whether it is Standard-legal, and what it costs in dust.
 */
@Composable
private fun CardFacts(card: Card, isStandardLegal: Boolean?) {
    val classes = card.classes.map { classLabel(it.slug) }.joinToString(" / ").ifBlank { classLabel("neutral") }
    val typeAndRarity = buildString {
        append(typeLabel(card.cardType.slug))
        card.rarity?.let { append(", ").append(rarityLabel(it.slug).lowercase()) }
    }
    val setSlug = card.cardSet?.slug
    val setName = expansionLabel(setSlug, card.cardSet?.name.orEmpty())
    val released = SetReleaseDates.label(setSlug)
    val craft = card.rarity?.craftingCost?.firstOrNull()?.takeIf { it > 0 }

    Plate(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.fillMaxWidth()) {
        FactRow(stringResource(Res.string.card_detail_class), classes, first = true)
        FactRow(stringResource(Res.string.card_detail_type), typeAndRarity, dot = card.rarity?.slug)
        if (setName.isNotBlank()) {
            FactRow(
                stringResource(Res.string.card_detail_set),
                if (released != null) "$setName · $released" else setName,
            )
        }
        if (isStandardLegal != null) {
            FactRow(
                label = stringResource(Res.string.card_detail_format),
                value = stringResource(
                    if (isStandardLegal) Res.string.card_detail_standard_legal else Res.string.card_detail_wild_only,
                ),
                valueColor = if (isStandardLegal) DeckBuilderColors.Success else DeckBuilderColors.Secondary,
            )
        }
        if (craft != null) {
            FactRow(
                stringResource(Res.string.card_detail_craft),
                stringResource(Res.string.card_detail_dust_value, craft),
            )
        }
      }
    }
}

@Composable
private fun FactRow(
    label: String,
    value: String,
    dot: String? = null,
    valueColor: androidx.compose.ui.graphics.Color? = null,
    first: Boolean = false,
) {
    val dividerColor = if (first) androidx.compose.ui.graphics.Color.Transparent else DeckBuilderColors.OutlineSoft
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    dividerColor,
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    strokeWidth = 1f,
                )
            }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(label, modifier = Modifier.weight(1f))
        if (dot != null) {
            RarityDot(dot)
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = value,
            style = AppType.rowName.copy(fontSize = 13.5.sp),
            color = valueColor ?: DeckBuilderColors.OnSurface,
            textAlign = TextAlign.End,
        )
    }

}

@Composable
private fun SubtitleRowLegacy(card: Card, isStandardLegal: Boolean?) {
    val localizedClass = card.classes.firstOrNull()?.let { classLabel(it.slug) }
    val localizedType = card.cardType.slug.takeIf { it.isNotBlank() }?.let { typeLabel(it) }
    val set = card.cardSet?.name
    val format = isStandardLegal?.let { formatLabel(if (it) GameFormat.STANDARD else GameFormat.WILD) }
    val artist = card.artistName?.let { stringResource(Res.string.card_detail_by_artist, it) }
    val parts = listOfNotNull(localizedClass, localizedType, set, format, artist)
    if (parts.isEmpty()) return
    Text(
        text = parts.joinToString(" \u00B7 "),
        style = MaterialTheme.typography.bodySmall,
        color = DeckBuilderColors.OnSurfaceDim,
        modifier = Modifier.padding(top = 4.dp),
    )
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

@Composable
private fun RelatedCardsSection(
    cards: List<Card>,
    isLoading: Boolean,
    onClick: (Card) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(Res.string.card_detail_related),
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

