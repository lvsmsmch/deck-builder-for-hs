package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.lvsmsmch.deckbuilder.R
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

private const val CARD_RENDER_ASPECT = 0.72f

@Composable
fun CardPreviewDialog(
    card: Card,
    onDismiss: () -> Unit,
) {
    val highUrl = card.image.takeIf { it.isNotBlank() }?.replace("/256x/", "/512x/") ?: card.cropImage
    val fallbackUrl = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val context = LocalContext.current
    val lowPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(fallbackUrl)
            .size(256, 384)
            .build(),
    )
    val highPainter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(highUrl ?: fallbackUrl)
            .size(512, 768)
            .build(),
    )
    val highState by highPainter.state.collectAsState()
    val backgroundInteraction = remember { MutableInteractionSource() }
    val contentInteraction = remember { MutableInteractionSource() }
    var targetScale by remember(card.id) { mutableFloatStateOf(1f) }
    var targetOffset by remember(card.id) { mutableStateOf(Offset.Zero) }
    var gestureActive by remember(card.id) { mutableStateOf(false) }
    var zoomChromeHidden by remember(card.id) { mutableStateOf(false) }
    val scale by animateFloatAsState(targetScale, tween(180), label = "preview-card-scale")
    val offset by animateOffsetAsState(targetOffset, tween(180), label = "preview-card-offset")
    val chromeAlpha by animateFloatAsState(
        targetValue = if (zoomChromeHidden) 0f else 1f,
        animationSpec = tween(160),
        label = "preview-card-chrome-alpha",
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
                .clickable(
                    interactionSource = backgroundInteraction,
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = 360.dp)
                    .clickable(
                        interactionSource = contentInteraction,
                        indication = null,
                        onClick = onDismiss,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(CARD_RENDER_ASPECT)
                        .zIndex(if (renderedScale > 1.01f) 10f else 0f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = renderedScale
                                scaleY = renderedScale
                                translationX = renderedOffset.x
                                translationY = renderedOffset.y
                            }
                            .zIndex(if (renderedScale > 1.01f) 12f else 0f)
                            .pointerInput(card.id) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    gestureActive = true
                                    do {
                                        val event = awaitPointerEvent()
                                        val pressedCount = event.changes.count { it.pressed }
                                        val zoom = event.calculateZoom()
                                        targetScale = (targetScale * zoom).coerceIn(1f, 3.2f)
                                        zoomChromeHidden = pressedCount >= 2 || targetScale > 1.01f
                                        if (pressedCount >= 2) {
                                            targetOffset += event.calculatePan()
                                        }
                                    } while (event.changes.any { it.pressed })
                                    gestureActive = false
                                    zoomChromeHidden = false
                                    targetScale = 1f
                                    targetOffset = Offset.Zero
                                }
                            },
                    ) {
                        Image(
                            painter = lowPainter,
                            contentDescription = card.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (highState is AsyncImagePainter.State.Success) {
                            Image(
                                painter = highPainter,
                                contentDescription = card.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    if (highState !is AsyncImagePainter.State.Success) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(DeckBuilderColors.Surface.copy(alpha = 0.72f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.card_image_loading),
                                color = DeckBuilderColors.OnSurface,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = (-8).dp)
                            .padding(end = 4.dp)
                            .size(44.dp)
                            .graphicsLayer { alpha = chromeAlpha }
                            .clip(RoundedCornerShape(99.dp))
                            .background(DeckBuilderColors.OnSurface)
                            .clickable(enabled = chromeAlpha > 0.5f, onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = DeckBuilderColors.Surface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                CardPreviewMetadata(
                    card = card,
                    modifier = Modifier
                        .zIndex(0f)
                        .graphicsLayer { alpha = chromeAlpha },
                )
            }
        }
    }
}

@Composable
private fun CardPreviewMetadata(card: Card, modifier: Modifier = Modifier) {
    val parts = listOfNotNull(
        card.classes.joinToString("/") { it.name }.takeIf { it.isNotBlank() },
        card.cardType.name.takeIf { it.isNotBlank() },
        card.cardSet?.name?.takeIf { it.isNotBlank() },
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = parts.joinToString(" • "),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = DeckBuilderColors.OnSurface,
            textAlign = TextAlign.Center,
        )
        card.flavorText?.takeIf { it.isNotBlank() }?.let { flavor ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = flavor,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = DeckBuilderColors.OnSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
