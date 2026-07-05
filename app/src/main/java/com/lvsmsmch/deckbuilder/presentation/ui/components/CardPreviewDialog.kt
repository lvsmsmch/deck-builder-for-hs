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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    onMore: () -> Unit,
) {
    val highUrl = card.image.takeIf { it.isNotBlank() }?.replace("/256x/", "/512x/") ?: card.cropImage
    val fallbackUrl = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(highUrl ?: fallbackUrl)
            .size(512, 768)
            .build(),
    )
    val state by painter.state.collectAsState()
    val backgroundInteraction = remember { MutableInteractionSource() }
    val contentInteraction = remember { MutableInteractionSource() }
    var targetScale by remember(card.id) { mutableFloatStateOf(1f) }
    var targetOffset by remember(card.id) { mutableStateOf(Offset.Zero) }
    var gestureActive by remember(card.id) { mutableStateOf(false) }
    val scale by animateFloatAsState(targetScale, tween(180), label = "preview-card-scale")
    val offset by animateOffsetAsState(targetOffset, tween(180), label = "preview-card-offset")
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
                        onClick = {},
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(CARD_RENDER_ASPECT),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painter,
                        contentDescription = card.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
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
                    if (state is AsyncImagePainter.State.Loading) {
                        Text(
                            text = stringResource(R.string.card_image_loading),
                            color = DeckBuilderColors.OnSurfaceDim,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 4.dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(DeckBuilderColors.OnSurface)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = DeckBuilderColors.Surface,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(DeckBuilderColors.OnSurface)
                        .clickable {
                            onDismiss()
                            onMore()
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.action_more),
                            color = DeckBuilderColors.Surface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = DeckBuilderColors.Surface,
                        )
                    }
                }
            }
        }
    }
}
