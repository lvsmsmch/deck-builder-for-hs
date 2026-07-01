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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.common.UiState
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.components.CardThumbnail
import com.lvsmsmch.deckbuilder.presentation.ui.components.rarityColor
import com.lvsmsmch.deckbuilder.presentation.ui.labels.classLabel
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.presentation.ui.labels.formatLabel
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
    var fullscreenImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        CardImagePanel(
            card = card,
            onOpenFullscreen = { fullscreenImage = true },
        )

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
            CardTextBlock(card.text)
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

    if (fullscreenImage) {
        FullscreenCardImage(card = card, onDismiss = { fullscreenImage = false })
    }
}

private const val CARD_RENDER_ASPECT = 0.72f

@Composable
private fun CardImagePanel(
    card: Card,
    onOpenFullscreen: () -> Unit,
) {
    val lowUrl = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val highUrl = card.image.takeIf { it.isNotBlank() }?.replace("/256x/", "/512x/") ?: card.cropImage
    val context = LocalContext.current
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
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(DeckBuilderColors.SurfaceContainer)
                .border(1.dp, DeckBuilderColors.OutlineSoft, RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenFullscreen),
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
        text = stringResource(R.string.card_image_loading),
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
            text = stringResource(R.string.card_image_error),
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
                text = stringResource(R.string.action_retry),
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
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(highUrl ?: fallbackUrl)
            .size(512, 768)
            .build(),
    )
    var targetScale by remember { mutableFloatStateOf(1f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeckBuilderColors.Surface)
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
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(card.id) {
                        awaitEachGesture {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                val softenedZoom = 1f + ((zoom - 1f) * 0.45f)
                                targetScale = (targetScale * softenedZoom).coerceIn(1f, 3.2f)
                                if (event.changes.count { it.pressed } >= 2) {
                                    targetOffset += event.calculatePan()
                                }
                            } while (event.changes.any { it.pressed })
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
                    contentDescription = stringResource(R.string.action_close),
                    tint = Color.White,
                )
            }
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
